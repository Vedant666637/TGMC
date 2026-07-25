import { Router, Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import { prisma } from '../../config/database';
import { generateAccessToken } from '../../config/jwt';
import { authMiddleware, adminOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';
import { uploadMiddleware } from '../../middleware/uploadMiddleware';
import path from 'path';
import { getOnlineParentCount } from '../../socket/socketServer';
export const adminRouter = Router();

// POST /api/admin/login
adminRouter.post('/login', async (req: Request, res: Response) => {
  const { email, password } = req.body;
  const admin = await prisma.admin.findUnique({ where: { email } });
  if (!admin || !(await bcrypt.compare(password, admin.passwordHash))) {
    res.status(401).json({ error: 'Invalid credentials' });
    return;
  }
  const accessToken = generateAccessToken({ userId: admin.id, email: admin.email, role: 'ADMIN' });
  res.json({ accessToken, role: 'ADMIN', userId: admin.id, email: admin.email });
});

// All routes below require admin auth
adminRouter.use(authMiddleware, adminOnlyMiddleware);

// GET /api/admin/stats — KPI dashboard overview
adminRouter.get('/stats', async (_req: AuthRequest, res: Response) => {
  const [parentCount, deviceCount, alertCount] = await Promise.all([
    prisma.parent.count(),
    prisma.childDevice.count(),
    prisma.alert.count()
  ]);
  const onlineDevices = await prisma.childDevice.count({ where: { isOnline: true } });
  res.json({
    totalParents: parentCount,
    totalDevices: deviceCount,
    onlineDevices,
    onlineParents: getOnlineParentCount(),
    totalAlerts: alertCount,
    generatedAt: new Date().toISOString()
  });
});

// GET /api/admin/users — searchable parent list
adminRouter.get('/users', async (req: AuthRequest, res: Response) => {
  const search = req.query.search as string;
  const page   = parseInt(req.query.page as string || '0');
  const users = await prisma.parent.findMany({
    where: search ? { email: { contains: search } } : undefined,
    select: {
      id: true,
      email: true,
      displayName: true,
      subscriptionPlan: true,
      subscriptionStatus: true,
      createdAt: true,
      devices: {
        select: {
          id: true,
          childName: true,
          deviceModel: true,
          isOnline: true
        }
      },
      _count: { select: { devices: true } }
    },
    orderBy: { createdAt: 'desc' },
    skip: page * 20,
    take: 20
  });
  res.json(users);
});

// GET /api/admin/storage
adminRouter.get('/storage', async (_req: AuthRequest, res: Response) => {
  // Mock storage stats since media files are stored on external service (S3, etc)
  res.json([
    { name: 'Camera Snapshots', size: 2.4, color: '#00E5FF' },
    { name: 'Audio Recordings', size: 1.8, color: '#6C63FF' },
    { name: 'Location History',  size: 0.3, color: '#00D68F' },
    { name: 'Content (Videos)',  size: 8.2, color: '#FFB347' },
    { name: 'Store Assets',      size: 1.1, color: '#FF4D6D' }
  ]);
});

// GET /api/admin/health
adminRouter.get('/health', async (_req: AuthRequest, res: Response) => {
  // Basic health — can be extended with real uptime/monitoring data
  const dbCheck = await prisma.$queryRaw`SELECT 1`.then(() => 'ok').catch(() => 'error');
  res.json({
    api: 'ok',
    database: dbCheck,
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// ── Phase 6: Educational Content Management ──

// GET /api/admin/content
adminRouter.get('/content', async (_req: AuthRequest, res: Response) => {
  const content = await prisma.educationalContent.findMany({
    orderBy: { publishedAt: 'desc' }
  });
  res.json(content);
});

// POST /api/admin/content
adminRouter.post(
  '/content',
  uploadMiddleware.fields([
    { name: 'media', maxCount: 1 },
    { name: 'thumbnail', maxCount: 1 }
  ]),
  async (req: AuthRequest, res: Response) => {
    const { type, title, description, category, durationSecs, url: providedUrl, thumbnailUrl: providedThumbnailUrl } = req.body;
    
    let url = providedUrl || '';
    let thumbnailUrl = providedThumbnailUrl || '';

    const files = req.files as { [fieldname: string]: Express.Multer.File[] } | undefined;
    
    if (files?.media && files.media.length > 0) {
      url = `/uploads/content/${files.media[0].filename}`;
    }
    if (files?.thumbnail && files.thumbnail.length > 0) {
      thumbnailUrl = `/uploads/content/${files.thumbnail[0].filename}`;
    }

    // For STATUS type, auto-set 24h expiry like WhatsApp
    const expiresAt = type === 'STATUS' ? new Date(Date.now() + 24 * 60 * 60 * 1000) : null;
    
    try {
      const content = await prisma.educationalContent.create({
        data: {
          type, 
          title, 
          url, 
          thumbnailUrl: thumbnailUrl || null, 
          description: description || null,
          category: category || 'GENERAL',
          durationSecs: durationSecs ? parseInt(durationSecs) : null,
          expiresAt,
          adminId: req.user!.userId
        }
      });
      res.status(201).json(content);
    } catch (error) {
      console.error('Error creating content:', error);
      res.status(500).json({ error: 'Failed to create content' });
    }
  }
);

// DELETE /api/admin/content/:id
adminRouter.delete('/content/:id', async (req: AuthRequest, res: Response) => {
  await prisma.educationalContent.delete({ where: { id: req.params.id } });
  res.status(204).send();
});

// GET /api/admin/store
adminRouter.get('/store', async (_req: AuthRequest, res: Response) => {
  const items = await prisma.storeItem.findMany({
    orderBy: { createdAt: 'desc' }
  });
  res.json(items);
});

// POST /api/admin/store
adminRouter.post(
  '/store',
  uploadMiddleware.single('image'),
  async (req: AuthRequest, res: Response) => {
    const { name, description, price, imageUrl: providedImageUrl } = req.body;
    let imageUrl = providedImageUrl || '';

    if (req.file) {
      imageUrl = `/uploads/store/${req.file.filename}`;
    }

    try {
      const item = await prisma.storeItem.create({
        data: {
          name, 
          description, 
          price: parseFloat(price), 
          imageUrl, 
          adminId: req.user!.userId
        }
      });
      res.status(201).json(item);
    } catch (error) {
      console.error('Error creating store item:', error);
      res.status(500).json({ error: 'Failed to create store item' });
    }
  }
);
