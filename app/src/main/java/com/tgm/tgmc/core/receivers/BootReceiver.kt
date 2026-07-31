package com.tgm.tgmc.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tgm.tgmc.core.services.MonitoringForegroundService
import com.tgm.tgmc.core.util.Constants

/**
 * Restarts the MonitoringForegroundService after device reboot.
 * Ensures the Child App's monitoring resumes automatically
 * without requiring parent to manually re-enable anything.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val serviceIntent = Intent(context, MonitoringForegroundService::class.java).apply {
                action = Constants.ACTION_START_MONITORING
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: SecurityException) {
                // On Android 14+, starting a Foreground Service of type camera/microphone 
                // will throw a SecurityException if the app doesn't have the permissions yet.
            }
        }
    }
}
