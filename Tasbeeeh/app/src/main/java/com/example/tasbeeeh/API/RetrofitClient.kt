package com.example.tasbeeeh.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.alquran.cloud/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val quranApi: QuranApiService by lazy {
        retrofit.create(QuranApiService::class.java)
    }
}