import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';
import { sendFcmNotification } from '../../config/firebase';

export const commandRouter = Router();

// ── Child registers/updates FCM token ────────────────────────────
// POST /api/devices/:deviceId/fcm-token
commandRouter.post('/:deviceId/fcm-token', authMiddleware, async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;
  const { fcmToken } = req.body;

  if (!fcmToken) {
    res.status(400).json({ error: 'fcmToken is required' });
    return;
  }

  try {
    await prisma.childDevice.update({
      where: { id: deviceId },
      data: { fcmToken, isOnline: true, lastSeenAt: new Date() }
    });
    console.log(`[FCM] Token registered for device ${deviceId}`);
    res.json({ success: true });
  } catch (error) {
    console.error(`[FCM] Failed to register token for device ${deviceId}:`, error);
    res.status(404).json({ error: 'Device not found' });
  }
});

// ── Child heartbeat (replaces persistent RTDB connection) ────────
// POST /api/devices/:deviceId/heartbeat
commandRouter.post('/:deviceId/heartbeat', authMiddleware, async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;

  try {
    await prisma.childDevice.update({
      where: { id: deviceId },
      data: { isOnline: true, lastSeenAt: new Date() }
    });
    res.json({ status: 'ok' });
  } catch {
    res.status(404).json({ error: 'Device not found' });
  }
});

// ── Parent sends command to child via FCM ────────────────────────
// POST /api/devices/:deviceId/command
commandRouter.post('/:deviceId/command', authMiddleware, parentOnlyMiddleware, async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;
  const { action, payload } = req.body;

  if (!action) {
    res.status(400).json({ error: 'action is required' });
    return;
  }

  try {
    // Get the child device's FCM token
    const device = await prisma.childDevice.findFirst({
      where: { id: deviceId, parentId: req.user!.userId }
    });

    if (!device) {
      res.status(404).json({ error: 'Device not found or not owned by you' });
      return;
    }

    if (!device.fcmToken) {
      res.status(400).json({ error: 'Device has no FCM token registered. Is it online?' });
      return;
    }

    // Send FCM data message to child device
    const sent = await sendFcmNotification(device.fcmToken, {
      data: {
        action,
        deviceId,
        payload: payload ? JSON.stringify(payload) : '{}',
        timestamp: Date.now().toString()
      }
    });

    if (sent) {
      console.log(`[CMD] Sent '${action}' command to device ${deviceId} via FCM`);
      res.json({ success: true, action, deviceId });
    } else {
      res.status(500).json({ error: 'Failed to deliver command via FCM' });
    }
  } catch (error) {
    console.error(`[CMD] Error sending command to device ${deviceId}:`, error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ── Parent pushes rule update to child via FCM ───────────────────
// POST /api/devices/:deviceId/push-rules
commandRouter.post('/:deviceId/push-rules', authMiddleware, parentOnlyMiddleware, async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;

  try {
    const device = await prisma.childDevice.findFirst({
      where: { id: deviceId, parentId: req.user!.userId }
    });

    if (!device) {
      res.status(404).json({ error: 'Device not found' });
      return;
    }

    // Gather all rules for this device
    const [blockRules, webFilters, schedules] = await Promise.all([
      prisma.appBlockRule.findMany({ where: { deviceId, isBlocked: true } }),
      prisma.webFilterRule.findMany({ where: { deviceId, isBlocked: true } }),
      prisma.timeSchedule.findMany({ where: { deviceId, isEnabled: true } })
    ]);

    const rulesPayload = {
      blockedApps: blockRules.map(r => r.packageName),
      blockedDomains: webFilters.filter(r => r.ruleType === 'DOMAIN').map(r => r.value),
      blockedKeywords: webFilters.filter(r => r.ruleType === 'KEYWORD').map(r => r.value),
      schedules: schedules.map(s => ({
        name: s.name,
        startHour: s.startHour, startMinute: s.startMinute,
        endHour: s.endHour, endMinute: s.endMinute,
        activeDays: s.activeDays
      }))
    };

    if (device.fcmToken) {
      await sendFcmNotification(device.fcmToken, {
        data: {
          action: 'rule_update',
          deviceId,
          payload: JSON.stringify(rulesPayload),
          timestamp: Date.now().toString()
        }
      });
      console.log(`[CMD] Pushed rules to device ${deviceId} via FCM`);
    }

    // Also update via Socket.IO for immediate delivery if connected
    try {
      const { io } = await import('../../socket/socketServer');
      io?.to(`device:${deviceId}`).emit('rule:update', rulesPayload);
    } catch { /* Socket.IO not critical */ }

    res.json({ success: true, rules: rulesPayload });
  } catch (error) {
    console.error(`[CMD] Error pushing rules to device ${deviceId}:`, error);
    res.status(500).json({ error: 'Internal server error' });
  }
});
