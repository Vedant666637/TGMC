package com.tgm.tgmc.feature.parent.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.domain.model.ChildDevice
import com.tgm.tgmc.core.domain.repository.AuthRepository
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.data.remote.FirebaseManager
import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentDashboardUiState(
    val devices: List<ChildDevice> = emptyList(),
    val selectedDevice: ChildDevice? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val authRepository: AuthRepository,
    private val firebaseManager: FirebaseManager,
    private val apiService: TgmcApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    init {
        firebaseManager.connect()
        loadDevices()

        // ── Observe real-time Online/Offline status from Firebase ──
        // This listens 24/7. When the child's phone turns off or loses internet,
        // Firebase automatically sets their status to "offline" and this updates the UI.
        viewModelScope.launch {
            firebaseManager.deviceOnlineStatus.collect { isOnline ->
                _uiState.update { state ->
                    val updatedSelected = state.selectedDevice?.copy(isOnline = isOnline)
                    val updatedDevices = state.devices.map { device ->
                        if (device.deviceId == state.selectedDevice?.deviceId) {
                            device.copy(isOnline = isOnline)
                        } else {
                            device
                        }
                    }
                    state.copy(
                        selectedDevice = updatedSelected,
                        devices = updatedDevices
                    )
                }
            }
        }
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getDevices()) {
                is Result.Success -> {
                    val devices = result.data
                    val defaultDevice = _uiState.value.selectedDevice ?: devices.firstOrNull()
                    // Start watching Firebase for real-time status of this device
                    defaultDevice?.let { dev -> firebaseManager.watchDevice(dev.deviceId) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            devices = devices,
                            selectedDevice = defaultDevice
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> { /* Handled via flow or ignore */ }
            }
        }
    }

    fun selectDevice(device: ChildDevice) {
        // Switch Firebase listener to the newly selected device
        firebaseManager.watchDevice(device.deviceId)
        _uiState.update { it.copy(selectedDevice = device) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            firebaseManager.disconnect()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
