package com.example.tasbeeeh.API

import SuraResponse
import SurahDetailResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiService {
    @GET("v1/surah")
    fun getAllSurahs(): Call<SuraResponse>

    @GET("v1/surah/{number}/{edition}")
    fun getSurah(
        @Path("number") number: Int,
        @Path("edition") edition: String
    ): Call<SurahDetailResponse>
}