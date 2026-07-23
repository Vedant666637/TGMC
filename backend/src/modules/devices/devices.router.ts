import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const devicesRouter = Router();
devicesRouter.use(authMiddleware, parentOnlyMiddleware);

// GET /api/devices
devicesRouter.get('/', async (req: AuthRequest, res: Response) => {
  const devices = await prisma.childDevice.findMany({
    where: { parentId: req.user!.userId },
    orderBy: { pairedAt: 'desc' }
  });
  res.json(devices);
});

// DELETE /api/devices/:deviceId  (unpair)
devicesRouter.delete('/:deviceId', async (req: AuthRequest, res: Response) => {
  const device = await prisma.childDevice.findFirst({
    where: { id: req.params.deviceId, parentId: req.user!.userId }
  });
  if (!device) { res.status(404).json({ error: 'Device not found' }); return; }
  await prisma.childDevice.delete({ where: { id: device.id } });
  res.status(204).send();
});

// GET /api/devices/:deviceId/activity (Phase 2 Activity Reports)
devicesRouter.get('/:deviceId/activity', async (req: AuthRequest, res: Response) => {
  // Mock data for Phase 2 MVP
  const activityData = {
    totalScreenTimeMinutes: 285, // 4h 45m
    appUsage: [
      { appName: "YouTube", packageName: "com.google.android.youtube", minutes: 105 },
      { appName: "TikTok", packageName: "com.zhiliaoapp.musically", minutes: 80 },
      { appName: "Chrome", packageName: "com.android.chrome", minutes: 45 },
      { appName: "Minecraft", packageName: "com.mojang.minecraftpe", minutes: 30 },
      { appName: "Instagram", packageName: "com.instagram.android", minutes: 25 }
    ]
  };
  res.json(activityData);
});

export const appBlockRouter = Router();
appBlockRouter.use(authMiddleware, parentOnlyMiddleware);

// GET /api/appblock/:deviceId/rules
appBlockRouter.get('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const rules = await prisma.appBlockRule.findMany({ where: { deviceId: req.params.deviceId } });
  res.json(rules);
});

// POST /api/appblock/:deviceId/rules
appBlockRouter.post('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const { packageName, appName, category, isBlocked } = req.body;
  const rule = await prisma.appBlockRule.upsert({
    where: { deviceId_packageName: { deviceId: req.params.deviceId, packageName } },
    update: { isBlocked, appName, category },
    create: { deviceId: req.params.deviceId, packageName, appName, category: category || 'OTHER', isBlocked: isBlocked ?? true }
  });
  res.json(rule);
});

// DELETE /api/appblock/:deviceId/rules/:packageName
appBlockRouter.delete('/:deviceId/rules/:packageName', async (req: AuthRequest, res: Response) => {
  await prisma.appBlockRule.deleteMany({
    where: { deviceId: req.params.deviceId, packageName: req.params.packageName }
  });
  res.status(204).send();
});
