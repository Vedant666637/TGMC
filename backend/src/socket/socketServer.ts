import { Server as HttpServer } from 'http';
import { Server as SocketServer, Socket } from 'socket.io';
import { verifyAccessToken } from '../config/jwt';
import { prisma } from '../config/database';

let io: SocketServer;

// Map: deviceId → socket.id (child device connections)
const deviceSocketMap = new Map<string, string>();
// Map: parentId → socket.id (parent connections)
const parentSocketMap = new Map<string, string>();

export function getOnlineParentCount(): number {
  return parentSocketMap.size;
}

export function initSocketServer(httpServer: HttpServer): void {
  io = new SocketServer(httpServer, {
    cors: {
      origin: process.env.CORS_ORIGIN || 'http://localhost:5173',
      methods: ['GET', 'POST']
    }
  });

  // Auth middleware for Socket.IO
  io.use((socket, next) => {
    const token = socket.handshake.auth?.token;
    if (!token) { next(new Error('Authentication required')); return; }
    try {
      (socket as any).user = verifyAccessToken(token);
      next();
    } catch {
      next(new Error('Invalid token'));
    }
  });

  io.on('connection', (socket: Socket) => {
    const user = (socket as any).user;
    console.log(`[WS] Connected: ${user.email} (${socket.id}) role: ${user.role}`);

    // Track active parents
    if (user.role === 'PARENT') {
      parentSocketMap.set(user.userId, socket.id);
    }

    // ── Child device joins ──────────────────────────────────────
    socket.on('device:join', async (data: { deviceId: string }) => {
      deviceSocketMap.set(data.deviceId, socket.id);
      socket.join(`device:${data.deviceId}`);
      await prisma.childDevice.update({
        where: { id: data.deviceId },
        data: { isOnline: true, lastSeenAt: new Date() }
      });
      console.log(`[WS] Device online: ${data.deviceId}`);
    });

    // ── Child sends real-time location ──────────────────────────
    socket.on('device:location', async (data: { deviceId: string; lat: number; lng: number; accuracy: number }) => {
      // Forward to parent(s) watching this device
      socket.to(`watching:${data.deviceId}`).emit('device:location', data);
      // Persist to DB (throttled — don't write every single update)
      await prisma.locationPing.create({
        data: { deviceId: data.deviceId, latitude: data.lat, longitude: data.lng, accuracy: data.accuracy }
      });
    });

    // ── Parent requests camera ──────────────────────────────────
    socket.on('camera:request', (data: { deviceId: string; camera: 'front' | 'rear' }) => {
      io.to(`device:${data.deviceId}`).emit('camera:request', data);
    });

    // ── Child sends camera frame ────────────────────────────────
    socket.on('camera:frame', (data: { deviceId: string; frameBase64: string }) => {
      socket.to(`watching:${data.deviceId}`).emit('camera:frame', data);
    });

    // ── Parent requests screen mirror ───────────────────────────
    socket.on('mirror:start', (data: { deviceId: string }) => {
      io.to(`device:${data.deviceId}`).emit('mirror:start', data);
    });

    socket.on('mirror:frame', (data: { deviceId: string; frameBase64: string }) => {
      socket.to(`watching:${data.deviceId}`).emit('mirror:frame', data);
    });

    // ── Parent requests audio ───────────────────────────────────
    socket.on('audio:start', (data: { deviceId: string }) => {
      io.to(`device:${data.deviceId}`).emit('audio:start', data);
    });

    socket.on('audio:chunk', (data: { deviceId: string; chunkBase64: string }) => {
      socket.to(`watching:${data.deviceId}`).emit('audio:chunk', data);
    });

    // ── Child triggers SOS ──────────────────────────────────────
    socket.on('sos:trigger', async (data: { deviceId: string; lat?: number; lng?: number }) => {
      console.log(`[WS] SOS triggered from device ${data.deviceId}`);
      // Notify all parents watching this device
      socket.to(`watching:${data.deviceId}`).emit('sos:trigger', data);
      // Persist alert
      await prisma.alert.create({
        data: {
          deviceId: data.deviceId,
          type: 'SOS',
          title: '🚨 SOS Alert',
          message: 'Your child triggered an emergency SOS alert.'
        }
      });
    });

    // ── Child reports app install/uninstall ───────────────────────
    socket.on('device:app_installed', async (data: { deviceId: string; packageName: string; appName: string }) => {
      console.log(`[WS] App installed on device ${data.deviceId}: ${data.appName}`);
      socket.to(`watching:${data.deviceId}`).emit('device:app_installed', data);
      await prisma.alert.create({
        data: {
          deviceId: data.deviceId,
          type: 'APP_INSTALLED',
          title: 'New App Installed',
          message: `${data.appName} (${data.packageName}) was installed.`
        }
      });
      // Ensure it appears in AppBlockScreen (default unblocked)
      await prisma.appBlockRule.upsert({
        where: { deviceId_packageName: { deviceId: data.deviceId, packageName: data.packageName } },
        update: {},
        create: { deviceId: data.deviceId, packageName: data.packageName, appName: data.appName, category: 'OTHER', isBlocked: false }
      });
    });

    socket.on('device:app_uninstalled', async (data: { deviceId: string; packageName: string }) => {
      console.log(`[WS] App uninstalled on device ${data.deviceId}: ${data.packageName}`);
      socket.to(`watching:${data.deviceId}`).emit('device:app_uninstalled', data);
      await prisma.alert.create({
        data: {
          deviceId: data.deviceId,
          type: 'APP_UNINSTALLED',
          title: 'App Uninstalled',
          message: `Package ${data.packageName} was removed.`
        }
      });
      await prisma.appBlockRule.deleteMany({
        where: { deviceId: data.deviceId, packageName: data.packageName }
      });
    });

    // ── Parent starts watching a device ─────────────────────────
    socket.on('watch:device', (data: { deviceId: string }) => {
      socket.join(`watching:${data.deviceId}`);
    });

    // ── Parent pushes rule update to child ──────────────────────
    socket.on('rule:update', (data: { deviceId: string; rules: any }) => {
      io.to(`device:${data.deviceId}`).emit('rule:update', data.rules);
    });

    // ── Disconnect ───────────────────────────────────────────────
    socket.on('disconnect', async () => {
      console.log(`[WS] Disconnected: ${user.email} (${socket.id})`);
      
      // Remove parent from active tracking
      if (user.role === 'PARENT') {
        parentSocketMap.delete(user.userId);
      }

      // Mark device offline if this was a child connection
      for (const [deviceId, socketId] of deviceSocketMap.entries()) {
        if (socketId === socket.id) {
          deviceSocketMap.delete(deviceId);
          await prisma.childDevice.update({
            where: { id: deviceId },
            data: { isOnline: false }
          });
        }
      }
    });
  });
}

export { io };
