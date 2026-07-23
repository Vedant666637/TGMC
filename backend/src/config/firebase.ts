import * as admin from 'firebase-admin';

const project_id = process.env.FIREBASE_PROJECT_ID;
const client_email = process.env.FIREBASE_CLIENT_EMAIL;
const database_url = process.env.FIREBASE_DATABASE_URL;
const storage_bucket = process.env.FIREBASE_STORAGE_BUCKET;

// Handle newlines in private key
const private_key = process.env.FIREBASE_PRIVATE_KEY
  ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
  : undefined;

let firebaseApp: admin.app.App | null = null;

if (project_id && client_email && private_key) {
  try {
    firebaseApp = admin.initializeApp({
      credential: admin.credential.cert({
        projectId: project_id,
        clientEmail: client_email,
        privateKey: private_key,
      }),
      databaseURL: database_url || `https://${project_id}-default-rtdb.firebaseio.com`,
      storageBucket: storage_bucket || `${project_id}.firebasestorage.app`,
    });
    console.log('🔥 Firebase Admin initialized successfully');
    console.log(`   📂 Project: ${project_id}`);
    console.log(`   🗄️ Database: ${database_url || `https://${project_id}-default-rtdb.firebaseio.com`}`);
    console.log(`   📦 Storage: ${storage_bucket || `${project_id}.firebasestorage.app`}`);
  } catch (error) {
    console.error('❌ Failed to initialize Firebase Admin:', error);
  }
} else {
  console.warn(
    '⚠️ Firebase configurations missing. Push notifications will be mocked.'
  );
  console.warn(
    '   Set FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, and FIREBASE_PRIVATE_KEY in .env'
  );
}

// ── Firebase Service Helpers ─────────────────────────────────────

/**
 * Get the Firebase Realtime Database reference.
 * Returns null if Firebase is not initialized.
 */
export function getDatabase(): admin.database.Database | null {
  if (!firebaseApp) return null;
  return admin.database();
}

/**
 * Get the Firebase Storage bucket.
 * Returns null if Firebase is not initialized.
 */
export function getStorageBucket(): admin.storage.Storage | null {
  if (!firebaseApp) return null;
  return admin.storage();
}

/**
 * Get the Firebase Auth instance.
 * Returns null if Firebase is not initialized.
 */
export function getAuth(): admin.auth.Auth | null {
  if (!firebaseApp) return null;
  return admin.auth();
}

/**
 * Send a push notification to a device via FCM.
 * If Firebase credentials are not set, it logs/mocks the notification instead.
 */
export async function sendFcmNotification(
  token: string,
  payload: {
    notification?: { title: string; body: string };
    data?: Record<string, string>;
  }
): Promise<boolean> {
  if (!firebaseApp) {
    console.log(
      `[MOCK FCM] Sending push notification to token ${token.substring(0, 10)}...:`,
      JSON.stringify(payload, null, 2)
    );
    return true;
  }

  try {
    const message: admin.messaging.Message = {
      token,
      notification: payload.notification,
      data: payload.data,
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          clickAction: 'TGMC_NOTIFICATION_CLICK',
        },
      },
    };

    const response = await admin.messaging().send(message);
    console.log(`🔥 FCM Notification sent: ${response}`);
    return true;
  } catch (error) {
    console.error('❌ FCM Notification failed:', error);
    return false;
  }
}

export default firebaseApp;
