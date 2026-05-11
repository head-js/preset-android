package com.lisitede.preset.preset.api

import com.google.gson.annotations.SerializedName

data class HttpBinResponse(
    val args: Map<String, String> = emptyMap(),
    val data: String = "",
    val files: Map<String, String> = emptyMap(),
    val form: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val json: Any? = null,
    val origin: String = "",
    val url: String = ""
)
