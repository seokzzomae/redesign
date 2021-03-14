package com.example.redesign.retrofit

import com.example.redesign.utils.API
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRetrofit {

    @GET("api")
    fun getCorpData(
            @Query("corp_name") corp_name : String): Call<JsonElement>

}