import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const scheduleRouter = Router();
scheduleRouter.use(authMiddleware, parentOnlyMiddleware);

scheduleRouter.get('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const schedules = await prisma.timeSchedule.findMany({ where: { deviceId: req.params.deviceId } });
  res.json(schedules);
});

scheduleRouter.post('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const { name, startHour, startMinute, endHour, endMinute, activeDays, isEnabled } = req.body;
  const schedule = await prisma.timeSchedule.create({
    data: {
      deviceId: req.params.deviceId,
      name,
      startHour,
      startMinute,
      endHour,
      endMinute,
      activeDays: JSON.stringify(activeDays),
      isEnabled: isEnabled ?? true
    }
  });
  res.status(201).json(schedule);
});

scheduleRouter.delete('/:deviceId/rules/:scheduleId', async (req: AuthRequest, res: Response) => {
  await prisma.timeSchedule.deleteMany({ where: { id: req.params.scheduleId, deviceId: req.params.deviceId } });
  res.status(204).send();
});
