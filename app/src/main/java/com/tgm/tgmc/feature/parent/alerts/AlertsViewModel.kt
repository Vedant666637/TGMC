package com.tgm.tgmc.feature.parent.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.domain.model.AlertItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class AlertsUiState(
    val alerts: List<AlertItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val apiService: TgmcApiService,
    private val dataStore: TgmcDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val deviceId = dataStore.deviceId.firstOrNull()
            if (deviceId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No device selected") }
                return@launch
            }
            try {
                val res = apiService.getAlerts(deviceId)
                if (res.isSuccessful) {
                    _uiState.update { it.copy(alerts = res.body() ?: emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load alerts") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Network error") }
            }
        }
    }

    fun markAsRead(alertId: String) {
        viewModelScope.launch {
            try {
                apiService.markAlertRead(alertId)
                _uiState.update { state ->
                    state.copy(alerts = state.alerts.map { if (it.id == alertId) it.copy(isRead = true) else it })
                }
            } catch (e: Exception) {
                // Ignore silent update errors
            }
        }
    }

    fun resolvePurchaseRequest(alertId: String, action: String) {
        viewModelScope.launch {
            try {
                val res = apiService.resolveAlert(alertId, mapOf("action" to action))
                if (res.isSuccessful) {
                    // Update alert list locally
                    _uiState.update { state ->
                        state.copy(alerts = state.alerts.map { 
                            if (it.id == alertId) it.copy(message = "${it.message}\n\nStatus: $action", isRead = true) else it 
                        })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
