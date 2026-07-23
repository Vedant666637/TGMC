import { Router, Response, Request } from 'express';
import { prisma } from '../../config/database';

export const childRouter = Router();

// POST /api/child/:deviceId/sos
childRouter.post('/:deviceId/sos', async (req: Request, res: Response) => {
  const { deviceId } = req.params;
  const { latitude, longitude } = req.body;
  
  const device = await prisma.childDevice.findUnique({ where: { id: deviceId } });
  if (!device) {
    res.status(404).json({ error: 'Device not found' });
    return;
  }

  const alert = await prisma.alert.create({
    data: {
      deviceId,
      type: 'SOS',
      title: '🚨 EMERGENCY SOS 🚨',
      message: `SOS triggered by ${device.childName}!${latitude && longitude ? ` Location: ${latitude}, ${longitude}` : ''}`,
      isRead: false
    }
  });

  res.status(201).json(alert);
});

// GET /api/child/content?type=STATUS|POST|REEL|VIDEO
childRouter.get('/content', async (req, res: Response) => {
  const type = req.query.type as string | undefined;

  const content = await prisma.educationalContent.findMany({
    where: {
      ...(type ? { type: type.toUpperCase() } : {}),
      // Exclude expired STATUS items
      OR: [
        { expiresAt: null },
        { expiresAt: { gt: new Date() } }
      ]
    },
    orderBy: { publishedAt: 'desc' }
  });
  res.json(content);
});

// GET /api/child/content/statuses — only active (non-expired) statuses
childRouter.get('/content/statuses', async (_req, res: Response) => {
  const statuses = await prisma.educationalContent.findMany({
    where: {
      type: 'STATUS',
      OR: [
        { expiresAt: null },
        { expiresAt: { gt: new Date() } }
      ]
    },
    orderBy: { publishedAt: 'desc' }
  });
  res.json(statuses);
});

// GET /api/child/content/feed — posts + reels + videos for the main Learn tab
childRouter.get('/content/feed', async (_req, res: Response) => {
  const feed = await prisma.educationalContent.findMany({
    where: { type: { in: ['POST', 'REEL', 'VIDEO'] } },
    orderBy: { publishedAt: 'desc' }
  });
  res.json(feed);
});

// GET /api/child/store
childRouter.get('/store', async (_req, res: Response) => {
  const items = await prisma.storeItem.findMany({
    orderBy: { createdAt: 'desc' }
  });
  res.json(items);
});

// POST /api/child/:deviceId/purchase-request/:itemId
childRouter.post('/:deviceId/purchase-request/:itemId', async (req: Request, res: Response) => {
  const { deviceId, itemId } = req.params;

  const device = await prisma.childDevice.findUnique({ where: { id: deviceId } });
  const item = await prisma.storeItem.findUnique({ where: { id: itemId } });

  if (!device || !item) {
    res.status(404).json({ error: 'Device or Item not found' });
    return;
  }

  // Create an alert for the parent
  const alert = await prisma.alert.create({
    data: {
      deviceId,
      type: 'PURCHASE_REQUEST',
      title: '🛒 Purchase Request',
      message: `${device.childName} requested to purchase "${item.name}" for $${item.price.toFixed(2)}.`,
      isRead: false
    }
  });

  // Here you would also emit a WebSocket event or push notification to the parent
  res.status(201).json({ success: true, alertId: alert.id });
});
