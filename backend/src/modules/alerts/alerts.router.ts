import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const alertsRouter = Router();
alertsRouter.use(authMiddleware);

alertsRouter.get('/:deviceId', async (req: AuthRequest, res: Response) => {
  const page = parseInt(req.query.page as string || '0');
  const alerts = await prisma.alert.findMany({
    where: { deviceId: req.params.deviceId },
    orderBy: { createdAt: 'desc' },
    skip: page * 20,
    take: 20
  });
  res.json(alerts);
});

alertsRouter.put('/:alertId/read', async (req: AuthRequest, res: Response) => {
  await prisma.alert.update({ where: { id: req.params.alertId }, data: { isRead: true } });
  res.status(204).send();
});

alertsRouter.post('/:alertId/resolve', async (req: AuthRequest, res: Response) => {
  const { action } = req.body; // 'APPROVED' | 'DENIED'
  // In a real implementation, 'APPROVED' might trigger a payment flow or grant item to child.
  // For now, we update the alert message to reflect the resolution and mark it read.
  const alert = await prisma.alert.findUnique({ where: { id: req.params.alertId } });
  if (!alert) { res.status(404).json({ error: 'Alert not found' }); return; }

  const updatedMessage = `${alert.message}\n\nStatus: ${action}`;
  await prisma.alert.update({
    where: { id: alert.id },
    data: {
      isRead: true,
      message: updatedMessage
    }
  });
  res.json({ success: true, status: action });
});
