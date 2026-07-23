package com.tgm.tgmc.feature.parent.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.remote.TgmcApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingUiState(
    val code: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val apiService: TgmcApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        generateCode()
    }

    fun generateCode() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.generatePairingCode()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { 
                        it.copy(isLoading = false, code = response.body()!!.code) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to generate code: ${response.code()}") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error: ${e.message}") 
                }
            }
        }
    }
}
