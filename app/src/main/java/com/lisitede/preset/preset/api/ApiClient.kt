package com.lisitede.preset.preset.api

import com.lisitede.preset.preset.utils.HttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {

    @POST("/post")
    suspend fun postTest(@Body body: Map<String, @JvmSuppressWildcards String>): HttpBinResponse

    companion object {
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://httpbin.org/")
            .client(HttpClient.instance)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val instance: ApiClient = retrofit.create(ApiClient::class.java)
    }
}
