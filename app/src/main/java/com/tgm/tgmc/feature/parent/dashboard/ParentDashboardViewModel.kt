package com.tgm.tgmc.feature.parent.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.domain.model.ChildDevice
import com.tgm.tgmc.core.domain.repository.AuthRepository
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.data.remote.SocketManager
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
    private val socketManager: SocketManager,
    private val apiService: TgmcApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    init {
        socketManager.setApiService(apiService)
        socketManager.connect()
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getDevices()) {
                is Result.Success -> {
                    val devices = result.data
                    val defaultDevice = _uiState.value.selectedDevice ?: devices.firstOrNull()
                    defaultDevice?.let { dev -> socketManager.watchDevice(dev.deviceId) }
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
        socketManager.watchDevice(device.deviceId)
        _uiState.update { it.copy(selectedDevice = device) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            socketManager.disconnect()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
