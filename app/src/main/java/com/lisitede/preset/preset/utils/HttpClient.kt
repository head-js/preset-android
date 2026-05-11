package com.lisitede.preset.preset.utils

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {

    val instance: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
}
