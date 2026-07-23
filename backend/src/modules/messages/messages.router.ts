import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const messagesRouter = Router();

// ── GET /api/messages/:deviceId ──────────────────────────────────────
// Fetch the message logs for a child device (Parent only)
messagesRouter.get('/:deviceId', authMiddleware, parentOnlyMiddleware, async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;

  try {
    // Ensure the parent owns this device
    const device = await prisma.childDevice.findFirst({
      where: { id: deviceId, parentId: req.user!.userId }
    });

    if (!device) {
      res.status(404).json({ error: 'Device not found' });
      return;
    }

    const messages = await prisma.messageLog.findMany({
      where: { deviceId },
      orderBy: { timestamp: 'desc' },
      take: 200 // Limit to recent messages
    });

    res.json(messages);
  } catch (error) {
    console.error(`[Messages] Error fetching logs for ${deviceId}:`, error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ── POST /api/messages/:deviceId ─────────────────────────────────────
// Log a new intercepted message (Child device calling this)
messagesRouter.post('/:deviceId', async (req: AuthRequest, res: Response) => {
  const { deviceId } = req.params;
  const { appName, sender, content, isIncoming } = req.body;

  if (!appName || !sender || !content) {
    res.status(400).json({ error: 'Missing required fields' });
    return;
  }

  try {
    const log = await prisma.messageLog.create({
      data: {
        deviceId,
        appName,
        sender,
        content,
        isIncoming: isIncoming !== undefined ? isIncoming : true
      }
    });

    // Optionally notify the parent in real-time via Socket.IO
    try {
      const { io } = await import('../../socket/socketServer');
      io?.to(`watching:${deviceId}`).emit('message:new', log);
    } catch { /* ignore socket error */ }

    res.json({ success: true, log });
  } catch (error) {
    console.error(`[Messages] Error logging message for ${deviceId}:`, error);
    res.status(500).json({ error: 'Internal server error' });
  }
});
