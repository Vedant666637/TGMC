import { Router, Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import { z } from 'zod';
import { prisma } from '../../config/database';
import { generateAccessToken, generateRefreshToken, verifyRefreshToken } from '../../config/jwt';
import { v4 as uuidv4 } from 'uuid';

export const authRouter = Router();

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});

const registerSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  displayName: z.string().min(2).optional()
});

// POST /api/auth/register
authRouter.post('/register', async (req: Request, res: Response) => {
  try {
    const { email, password, displayName } = registerSchema.parse(req.body);
    const existing = await prisma.parent.findUnique({ where: { email } });
    if (existing) {
      res.status(409).json({ error: 'Email already registered' });
      return;
    }
    const passwordHash = await bcrypt.hash(password, 12);
    const parent = await prisma.parent.create({
      data: { email, passwordHash, displayName }
    });
    const accessToken  = generateAccessToken({ userId: parent.id, email: parent.email, role: 'PARENT' });
    const refreshToken = generateRefreshToken(parent.id);
    await prisma.refreshToken.create({
      data: {
        token: refreshToken,
        parentId: parent.id,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });
    res.status(201).json({ accessToken, refreshToken, role: 'PARENT', userId: parent.id, email: parent.email });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      res.status(400).json({ error: 'Validation failed', details: err.errors });
      return;
    }
    throw err;
  }
});

// POST /api/auth/login
authRouter.post('/login', async (req: Request, res: Response) => {
  try {
    const { email, password } = loginSchema.parse(req.body);
    const parent = await prisma.parent.findUnique({ where: { email } });
    if (!parent || !(await bcrypt.compare(password, parent.passwordHash))) {
      res.status(401).json({ error: 'Invalid email or password' });
      return;
    }
    const accessToken  = generateAccessToken({ userId: parent.id, email: parent.email, role: 'PARENT' });
    const refreshToken = generateRefreshToken(parent.id);
    await prisma.refreshToken.create({
      data: {
        token: refreshToken,
        parentId: parent.id,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });
    res.json({ accessToken, refreshToken, role: 'PARENT', userId: parent.id, email: parent.email });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      res.status(400).json({ error: 'Validation failed', details: err.errors });
      return;
    }
    throw err;
  }
});

// POST /api/auth/refresh
authRouter.post('/refresh', async (req: Request, res: Response) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    res.status(400).json({ error: 'Refresh token required' });
    return;
  }
  try {
    const { userId } = verifyRefreshToken(refreshToken);
    const stored = await prisma.refreshToken.findFirst({
      where: { token: refreshToken, parentId: userId, expiresAt: { gt: new Date() } }
    });
    if (!stored) {
      res.status(401).json({ error: 'Invalid or expired refresh token' });
      return;
    }
    const parent = await prisma.parent.findUnique({ where: { id: userId } });
    if (!parent) {
      res.status(401).json({ error: 'Account not found' });
      return;
    }
    const newAccessToken  = generateAccessToken({ userId: parent.id, email: parent.email, role: 'PARENT' });
    const newRefreshToken = generateRefreshToken(parent.id);
    // Rotate refresh token
    await prisma.$transaction([
      prisma.refreshToken.delete({ where: { id: stored.id } }),
      prisma.refreshToken.create({
        data: { token: newRefreshToken, parentId: parent.id, expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) }
      })
    ]);
    res.json({ accessToken: newAccessToken, refreshToken: newRefreshToken, role: 'PARENT', userId: parent.id, email: parent.email });
  } catch {
    res.status(401).json({ error: 'Invalid refresh token' });
  }
});

// POST /api/auth/forgot-password
authRouter.post('/forgot-password', async (req: Request, res: Response) => {
  const { email } = req.body;
  // Always return 200 to prevent email enumeration
  if (!email) { res.status(400).json({ error: 'Email required' }); return; }
  const parent = await prisma.parent.findUnique({ where: { email } });
  if (parent) {
    // TODO: Generate reset token, send email via nodemailer
    console.log(`[Auth] Password reset requested for ${email}`);
  }
  res.json({ message: 'If that email is registered, a reset link has been sent.' });
});
