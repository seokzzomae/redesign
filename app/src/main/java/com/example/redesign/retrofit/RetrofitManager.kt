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

    fun serachCorpClass(corp_code : String?, bgn_de : String?, last_reprt_at: String?, completion : (RESPONSE_STATE, String) -> Unit){

        val corp_code_new : String = corp_code ?: ""
        val bgn_de_new : String = bgn_de ?: ""
        val last_reprt_at_new : String = last_reprt_at ?: ""

        val call : Call<JsonElement> = iRetrofit?.getCorpClass(corp_code = corp_code_new, bgn_de= bgn_de_new, last_reprt_at = last_reprt_at_new ).let{
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

    fun serachCorpData(corp_code : String?, bsns_year : String?, reprt_code: String?, completion : (RESPONSE_STATE, String) -> Unit){

        val corp_code_new : String = corp_code ?: ""
        val bsns_year_new : String = bsns_year ?: ""
        val reprt_code_new : String = reprt_code ?: ""

        val call : Call<JsonElement> = iRetrofit?.getCorpData(corp_code = corp_code_new, bsns_year= bsns_year_new, reprt_code = reprt_code_new ).let{
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