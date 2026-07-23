import { Router, Response } from 'express';
import { z } from 'zod';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';
import { v4 as uuidv4 } from 'uuid';

export const pairingRouter = Router();
pairingRouter.use(authMiddleware);

// POST /api/pairing/generate  (parent generates invite code)
pairingRouter.post('/generate', parentOnlyMiddleware, async (req: AuthRequest, res: Response) => {
  const code = `${randomCode(4)}-${randomCode(4)}`;
  const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes
  await prisma.pairingCode.create({
    data: { id: uuidv4(), code, parentId: req.user!.userId, expiresAt }
  });
  res.json({ code, qrData: JSON.stringify({ code, parentId: req.user!.userId }), expiresAt: expiresAt.getTime() });
});

// POST /api/pairing/activate  (child activates code)
pairingRouter.post('/activate', async (req: AuthRequest, res: Response) => {
  const schema = z.object({ code: z.string(), deviceName: z.string().optional(), deviceModel: z.string() });
  const { code, deviceName, deviceModel } = schema.parse(req.body);

  const pairingCode = await prisma.pairingCode.findFirst({
    where: { code, isUsed: false, expiresAt: { gt: new Date() } }
  });
  if (!pairingCode) {
    res.status(400).json({ error: 'Invalid or expired pairing code' });
    return;
  }
  const [device, parent] = await prisma.$transaction([
    prisma.childDevice.create({
      data: {
        id: uuidv4(),
        parentId: pairingCode.parentId,
        childName: deviceName || 'Child',
        deviceModel
      }
    }),
    prisma.parent.findUniqueOrThrow({ where: { id: pairingCode.parentId } })
  ]);
  await prisma.pairingCode.update({ where: { id: pairingCode.id }, data: { isUsed: true } });
  res.json({ deviceId: device.id, parentEmail: parent.email });
});

function randomCode(length: number): string {
  return Math.random().toString(36).toUpperCase().slice(2, 2 + length).padEnd(length, '0');
}
