import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const locationRouter = Router();
locationRouter.use(authMiddleware);

// GET history (parent)
locationRouter.get('/:deviceId/history', async (req: AuthRequest, res: Response) => {
  const limit = parseInt(req.query.limit as string || '50');
  const history = await prisma.locationPing.findMany({
    where: { deviceId: req.params.deviceId },
    orderBy: { createdAt: 'desc' },
    take: limit
  });
  res.json(history);
});

// POST ping (child device sends location)
locationRouter.post('/:deviceId/ping', async (req: AuthRequest, res: Response) => {
  const { latitude, longitude, accuracy, address } = req.body;
  const ping = await prisma.locationPing.create({
    data: { deviceId: req.params.deviceId, latitude, longitude, accuracy, address }
  });
  // Also update device lastSeenAt and mark as online
  await prisma.childDevice.update({
    where: { id: req.params.deviceId },
    data: { isOnline: true, lastSeenAt: new Date() }
  });
  res.status(201).json(ping);
});

// GET geofences
locationRouter.get('/:deviceId/geofences', async (req: AuthRequest, res: Response) => {
  const geofences = await prisma.geofence.findMany({ where: { deviceId: req.params.deviceId } });
  res.json(geofences);
});

// POST geofence
locationRouter.post('/:deviceId/geofences', async (req: AuthRequest, res: Response) => {
  const { name, latitude, longitude, radiusMeters, alertOnEnter, alertOnExit } = req.body;
  const geofence = await prisma.geofence.create({
    data: { deviceId: req.params.deviceId, name, latitude, longitude, radiusMeters, alertOnEnter, alertOnExit }
  });
  res.status(201).json(geofence);
});

// DELETE geofence
locationRouter.delete('/:deviceId/geofences/:geofenceId', async (req: AuthRequest, res: Response) => {
  await prisma.geofence.deleteMany({ where: { id: req.params.geofenceId, deviceId: req.params.deviceId } });
  res.status(204).send();
});
