# TGM-C Parental Control Platform

This repository contains the complete TGM-C platform, consisting of three main components:
1. **Android Application (`/app`)**: Contains both the Parent Dashboard and the Child Monitoring agent.
2. **Node.js Backend (`/backend`)**: Handles all REST API requests, database interactions (Prisma), and real-time WebSocket routing (Socket.io).
3. **React Admin Dashboard (`/admin-dashboard`)**: A web interface for internal platform administrators to monitor health, manage accounts, and publish Educational Content.

## 🚀 Getting Started

### 1. Start the Backend Server
The backend requires Node.js and uses Prisma with an SQLite database (for MVP testing).

```bash
cd backend
npm install
npx prisma generate
npx prisma db push
npm run dev
```
*The backend will run on `http://localhost:3000`. Keep this running.*

### 2. Run the React Admin Dashboard
The admin dashboard is a Vite React application.

```bash
cd admin-dashboard
npm install
npm run dev
```
*The dashboard will run on `http://localhost:5173`. You can log in using the Admin credentials seeded in your backend.*

### 3. Compile the Android App
The Android app is built with native Kotlin, Jetpack Compose, and Hilt for Dependency Injection.

1. Open Android Studio.
2. Select **File > Open** and choose the `TGMC` root folder (or the `app` folder specifically).
3. Allow Gradle to sync dependencies.
4. Run the app on a physical Android device or an Emulator.

*Note: For testing the Live Media features (Camera, Screen Mirror, Audio), it is highly recommended to use **two physical Android devices**, as emulators often do not support hardware camera or microphone properly.*

## 🧪 Testing the Live Media Flow
1. Install the APK on **Device A (Parent)** and **Device B (Child)**.
2. On Device A, select "Parent Mode", sign up, and generate a pairing QR/Code.
3. On Device B, select "Child Mode" and enter the pairing code.
4. On Device B, grant all requested permissions (Accessibility, Location, Camera, Microphone, Screen Record).
5. On Device A, open the Dashboard and select **Remote Camera**, **Live Audio**, or **Screen Mirroring**.
6. Press **Start**. You should instantly see a notification pop up on Device B, and the media stream will begin playing on Device A!

## 📜 Compliance Note
As per the PRD §6.2, the Android application does **not** operate in stealth mode. When monitoring features (Camera/Audio/Screen) are active, a persistent notification will be displayed on the Child device.
