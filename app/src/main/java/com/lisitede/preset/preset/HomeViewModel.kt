package com.lisitede.preset.preset

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lisitede.preset.preset.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val prefs: SharedPreferences) : ViewModel() {

    companion object {
        private const val KEY_COUNTER = "counter"
    }

    private val _count = MutableStateFlow(prefs.getInt(KEY_COUNTER, 0))
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _httpPostState = MutableStateFlow(HttpPostState())
    val httpPostState: StateFlow<HttpPostState> = _httpPostState.asStateFlow()

    private val apiClient = ApiClient.instance

    fun increment() {
        _count.value++
        prefs.edit().putInt(KEY_COUNTER, _count.value).apply()
    }

    fun decrement() {
        _count.value--
        prefs.edit().putInt(KEY_COUNTER, _count.value).apply()
    }

    fun sendPost(data: Map<String, @JvmSuppressWildcards String>) {
        viewModelScope.launch {
            _httpPostState.value = _httpPostState.value.copy(isLoading = true, error = null)
            try {
                val response = withContext(Dispatchers.IO) {
                    apiClient.postTest(data)
                }
                _httpPostState.value = HttpPostState(
                    isLoading = false,
                    response = "origin=${response.origin}, url=${response.url}, data=${response.data}"
                )
            } catch (e: Exception) {
                _httpPostState.value = HttpPostState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    class Factory(private val prefs: SharedPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(prefs) as T
    }
}
