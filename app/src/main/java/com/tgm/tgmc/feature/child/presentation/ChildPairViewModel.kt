package com.tgm.tgmc.feature.child.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.core.domain.repository.PairingRepository
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import com.tgm.tgmc.core.data.remote.SocketManager
import com.tgm.tgmc.core.data.remote.TgmcApiService

data class ChildPairUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val parentEmail: String? = null
)

@HiltViewModel
class ChildPairViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val dataStore: TgmcDataStore,
    private val socketManager: SocketManager,
    private val tgmcApiService: TgmcApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildPairUiState())
    val uiState: StateFlow<ChildPairUiState> = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code, error = null) }
    }

    fun activateCode() {
        val code = _uiState.value.code.trim()
        if (code.isBlank() || !code.contains("-")) {
            _uiState.update { it.copy(error = "Enter a valid pairing code (e.g. XXXX-XXXX)") }
            return
        }

        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        val deviceName = Build.DEVICE

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairingRepository.activateCode(code, deviceModel, deviceName)) {
                is Result.Success -> {
                    val (deviceId, parentEmail) = result.data
                    
                    // Save to secure datastore
                    dataStore.saveDeviceId(deviceId)
                    dataStore.markPaired(true)
                    
                    // Also save mock credentials or mark role as CHILD
                    dataStore.saveAuthTokens(
                        accessToken = "child-token-$deviceId",
                        refreshToken = "child-refresh-$deviceId",
                        role = UserRole.CHILD,
                        userId = deviceId,
                        email = parentEmail
                    )

                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = true, 
                            parentEmail = parentEmail
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

    fun approveConsent() {
        viewModelScope.launch {
            dataStore.markConsentGiven()
        }
    }

    fun triggerSos() {
        viewModelScope.launch {
            val deviceId = dataStore.deviceId.firstOrNull() ?: return@launch
            try {
                // You can also add location data if you have access to it.
                // For MVP Phase 2, we just trigger the HTTP endpoint.
                socketManager.triggerSos(deviceId) // keeping the socket trigger for real-time siren
                
                // Call API to create a persistent alert in the database
                tgmcApiService.triggerSos(deviceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
