package com.example.redesign.retrofit

import com.example.redesign.utils.API
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRetrofit {

    @GET(API.SEARCH_CORP_CLASS)
    fun getCorpClass(
            @Query("corp_code") corp_code :String,
            @Query("bgn_de") bgn_de : String,
            @Query("last_reprt_at") last_reprt_at : String): Call<JsonElement>

    @GET(API.SEARCH_CORP_DATA)
    fun getCorpData(
            @Query("corp_code") corp_code : String,
            @Query("bsns_year") bsns_year : String,
            @Query("reprt_code") reprt_code : String): Call<JsonElement>

}