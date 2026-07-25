package com.tgm.tgmc.feature.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.TgmcApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val apiService: TgmcApiService,
    private val dataStore: TgmcDataStore
) : ViewModel() {

    // Parent triggering an alert on the Child's phone
    fun triggerAlertOnChild(deviceId: String) {
        viewModelScope.launch {
            try {
                // Send FCM command to the child device to play an alarm
                val request = mapOf(
                    "action" to "play_alarm",
                    "payload" to "{}"
                )
                apiService.sendCommand(deviceId, request)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Child triggering an SOS alert on the Parent's phone
    fun triggerSosToParent() {
        viewModelScope.launch {
            try {
                val deviceId = dataStore.deviceId.firstOrNull() ?: return@launch
                // Post to the SOS endpoint
                apiService.triggerSos(deviceId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
