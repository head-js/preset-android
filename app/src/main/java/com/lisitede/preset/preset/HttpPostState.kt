package com.lisitede.preset.preset

data class HttpPostState(
    val isLoading: Boolean = false,
    val response: String = "",
    val error: String? = null
)
