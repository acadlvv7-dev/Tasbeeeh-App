package com.example.tasbeeeh

import com.example.tasbeeeh.API.QuranApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitClient {
    private const val BASE_URL = "https://api.alquran.cloud/"

    val instance: QuranApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApiService::class.java)
    }
}