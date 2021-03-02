package com.example.redesign.retrofit

import android.util.Log
import com.google.gson.JsonElement
import com.example.redesign.utils.API
import com.example.redesign.utils.Constants.TAG
import com.example.redesign.utils.RESPONSE_STATE
import com.example.redesign.retrofit.IRetrofit
import retrofit2.Call
import retrofit2.Response

class RetrofitManager {

    companion object {
        val instance = RetrofitManager()
    }
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun serachCorpData(corp_name : String?, completion : (RESPONSE_STATE, String) -> Unit){

        val corp_name_new : String = corp_name ?: ""

        val call : Call<JsonElement> = iRetrofit?.getCorpData(corp_name = corp_name_new).let{
            it
        } ?: return

        call.enqueue(object : retrofit2.Callback<JsonElement>{

            // 응답 실패시
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "RetrofitManager - onFailure() called / t: $t")

                completion(RESPONSE_STATE.FAIL, t.toString())
            }

            // 응답 성공시
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "RetrofitManager - onResponse() called / response : ${response.body()}")

                completion(RESPONSE_STATE.OKAY ,response.body().toString())

            }

        })

    }



}