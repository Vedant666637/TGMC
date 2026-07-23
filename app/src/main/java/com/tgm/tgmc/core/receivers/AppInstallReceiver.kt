package com.tgm.tgmc.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.FirebaseManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for tracking when the child installs or uninstalls an app.
 * Relays this information to the backend via WebSocket (SocketManager).
 */
@AndroidEntryPoint
class AppInstallReceiver : BroadcastReceiver() {

    @Inject lateinit var firebaseManager: FirebaseManager
    @Inject lateinit var dataStore: TgmcDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AppInstallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val packageName = intent.data?.encodedSchemeSpecificPart ?: return

        scope.launch {
            val deviceId = dataStore.deviceId.firstOrNull()
            if (deviceId == null) {
                Log.w(TAG, "No deviceId found. Ignoring package event.")
                return@launch
            }

            // Connect socket if not connected (just in case service was killed)
            if (!firebaseManager.isConnected.value) {
                firebaseManager.connect()
            }

            when (action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                        Log.d(TAG, "Package $packageName is just updating. Ignoring.")
                        return@launch
                    }
                    val appName = getAppName(context, packageName)
                    Log.i(TAG, "New app installed: $appName ($packageName)")
                    firebaseManager.sendAppInstalled(deviceId, packageName, appName)
                }
                Intent.ACTION_PACKAGE_FULLY_REMOVED, Intent.ACTION_PACKAGE_REMOVED -> {
                    if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                        Log.d(TAG, "Package $packageName is being replaced. Ignoring.")
                        return@launch
                    }
                    Log.i(TAG, "App uninstalled: $packageName")
                    firebaseManager.sendAppUninstalled(deviceId, packageName)
                }
            }
        }
    }

    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            pm.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name if not found
        }
    }
}
