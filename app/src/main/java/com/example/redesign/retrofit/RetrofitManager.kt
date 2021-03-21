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
    // TODO 원호 COMMENT: iRetrofit 과 RetrofitClient.getClient가 null일 수 있나요? 없으면 ? 없이 가는게 안전할 것 같습니다.
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    // TODO 원호 COMMENT: 위와 마찬가지로 corp_name 에 아무 것도 안 넣는 걸 다른데서 잡고 들어가는게 낫지 않을까 합니다.
    // 그러면 corp_name 을 바로 getCorpData 에 인자로 넣어줄 수도 있구요.
    // (ArgTypes) -> ReturnType 코틀린 함수 인자로 함수를 넣는 방식
    // https://thdev.tech/kotlin/2017/10/02/Kotlin-Higher-Order-Function/
    fun searchCorpData(corp_name : String?, completion : (RESPONSE_STATE, String) -> Unit){

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