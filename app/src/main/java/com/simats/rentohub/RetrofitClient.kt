package com.simats.rentohub

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://qjvq60kp-80.inc1.devtunnels.ms/rentohub/"

    // 1. Create a custom OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Wait 60s to connect
        .readTimeout(60, TimeUnit.SECONDS)    // Wait 60s for server to send data
        .writeTimeout(60, TimeUnit.SECONDS)   // Wait 60s to send data
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 2. Attach the custom client here
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
