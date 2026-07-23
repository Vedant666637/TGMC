import 'dotenv/config';
import './config/firebase'; // Initialize Firebase Admin SDK on startup
import express from 'express';
import { createServer } from 'http';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { initSocketServer } from './socket/socketServer';
import { authRouter } from './modules/auth/auth.router';
import { pairingRouter } from './modules/pairing/pairing.router';
import { devicesRouter } from './modules/devices/devices.router';
import { appBlockRouter } from './modules/appblock/appblock.router';
import { scheduleRouter } from './modules/schedule/schedule.router';
import { locationRouter } from './modules/location/location.router';
import { alertsRouter } from './modules/alerts/alerts.router';
import { adminRouter } from './modules/admin/admin.router';
import { childRouter } from './modules/child/child.router';
import { webfilterRouter } from './modules/webfilter/webfilter.router';
import { errorHandler } from './middleware/errorHandler';
import { requestLogger } from './middleware/requestLogger';

const app = express();
const httpServer = createServer(app);

// ── Middleware ────────────────────────────────────────────────────
app.use(helmet());
app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:5173',
  credentials: true
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(requestLogger);

// Serve static uploaded files
import path from 'path';
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100,
  message: { error: 'Too many requests. Please try again later.' }
});
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: { error: 'Too many auth attempts. Try again in 15 minutes.' }
});
app.use('/api/', limiter);
app.use('/api/auth/', authLimiter);

import { commandRouter } from './modules/commands/command.router';
import { messagesRouter } from './modules/messages/messages.router';

// ── Routes ────────────────────────────────────────────────────────
app.use('/api/auth',    authRouter);
app.use('/api/pairing', pairingRouter);
app.use('/api/devices', devicesRouter);
app.use('/api/appblock', appBlockRouter);
app.use('/api/schedule', scheduleRouter);
app.use('/api/location', locationRouter);
app.use('/api/alerts',  alertsRouter);
app.use('/api/admin',   adminRouter);
app.use('/api/child',   childRouter);
app.use('/api/webfilter', webfilterRouter);
app.use('/api/commands', commandRouter);
app.use('/api/messages', messagesRouter);

// Health check
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString(), version: '1.0.0' });
});

// ── Error Handler ─────────────────────────────────────────────────
app.use(errorHandler);

// ── WebSocket (Socket.IO) ─────────────────────────────────────────
initSocketServer(httpServer);

// ── Start ─────────────────────────────────────────────────────────
const PORT = parseInt(process.env.PORT || '3000');
httpServer.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 TGM-C Backend running on port ${PORT}`);
  console.log(`📡 WebSocket ready on ws://0.0.0.0:${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV || 'development'}`);
});

export default app;
