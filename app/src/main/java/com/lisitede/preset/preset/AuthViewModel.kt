package com.lisitede.preset.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val tokenStorage: TokenStorage,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        restoreFromStorage()
    }

    private fun restoreFromStorage() {
        val token = tokenStorage.getToken()
        val username = tokenStorage.getUsername()
        if (token != null && username != null) {
            _state.value = AuthState(isLoggedIn = true, username = username, token = token)
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.login(username, password)
            if (result.isSuccess) {
                val (token, user) = result.getOrThrow()
                tokenStorage.saveToken(token, user)
                _state.value = AuthState(isLoggedIn = true, username = user, token = token)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun logout() {
        tokenStorage.clear()
        _state.value = AuthState()
    }

    class Factory(
        private val tokenStorage: TokenStorage,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(tokenStorage, authRepository) as T
    }
}
