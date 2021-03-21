package com.example.redesign

import android.content.Intent
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.redesign.models.DelistingConditions
import com.example.redesign.retrofit.RetrofitManager
import com.example.redesign.utils.Constants.TAG
import com.example.redesign.utils.RESPONSE_STATE
import kotlinx.android.synthetic.main.activity_result.*
import org.json.JSONArray
import org.json.JSONObject

class Result : AppCompatActivity() {
    lateinit var jArray: JSONArray
    lateinit var companyName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Result onCreate initiated")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        companyName = intent.getStringExtra("companyName")?:run{
            Log.i(TAG, "Result getStringExtra 에 null companyName 입력.")
            throw IllegalArgumentException("company name is null")
        }   //회사 이름 받음
        resultname.text = companyName

        val (situation, maxScore) = getCorpDataFromJSON()
//        val (situation, maxScore) = getCorpData()

        var text1 ="관련 설명 쓰는 곳곳!!!"

        // score.progress 는 Int 만 받을 수 있습니다. 소수 아래는 버리겠지만 프로그레스 바 크기가 그렇게 많이 달라지진 않을 것 같아요.
        score.progress = maxScore.toInt()
        detail_opinion.text = text1
        back.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        when{
            maxScore >= 75 ->{
                resultImage.setBackgroundResource(
                    R.drawable.bad_result)
                yuhumdo.text = "매우 위험"
                opinion.text = "투자를 삼가세요!"
            }

            maxScore > 25 ->{
                resultImage.setBackgroundResource(
                        R.drawable.medium_result)
                yuhumdo.text = "위험"
                opinion.text = "자산이 위험해요!"
            }

            maxScore >= 0 ->{
                resultImage.setBackgroundResource(
                    R.drawable.good_result)
                yuhumdo.text = "양호"
                val text = situation.conditionName + "은 안 될것 같아요!"
                opinion.text = text
            }
        }
    }

    private fun parseRetrofitResponse(response : RESPONSE_STATE, string: String) : Unit{
        when (response){
            RESPONSE_STATE.FAIL->{
                // Fail 뜨면 getCorpData를 그냥 빠져나가야 할 것 같아요
            }
            RESPONSE_STATE.OKAY->{
                val jObject = JSONObject(string)
                jArray = jObject.getJSONArray("rprts")
            }
        }
    }
    private fun getCorpDataFromJSON() : Pair<DelistingConditions, Double>{
        // assets에 있는 데이터를 활용하기 위해서 AssetManager 생성
        // resources를 사용하려면 클래스가 AppCompatActivity() 을 상속받아야해서
        // InvestEvaluator 에서 사용 못하고 여기서 json 접근하게 만듬
        val assetManager: AssetManager = resources.assets
        // jsonfile 접근
        val inputStream= assetManager.open("jsonfile3")
        // jsonfile 에서 스트링을 읽어서 오브젝트
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jObject = JSONObject(jsonString)
        jArray = jObject.getJSONArray("rprts")
        val companyClass = jObject.getString("corp_cls")
        Log.i(TAG, companyClass)
        val investEvaluator = InvestEvaluator(jArray, companyClass)

        // 4개 검사 결과를 ArrayList<DelistingConditions>, ArrayList<Double> 형태
        // 4개 테스트 관리/상폐조건 상태 어레이리스트, 4개 테스트 점수 어레이리스트 로 받아서
        // 제일 심한 결과를 출력하는 과정을 짰습니다

        // situation == DelistingConditions.CARE 면 관리조건, DelistingConditions.DELIST 면 상폐조건
        val (evaluationSituations, evaluationScores) = investEvaluator.evaluateCorp()
        var situation = DelistingConditions.CARE // 관리 조건 , 상폐 조건
        var maxScore = 0.0 // 가장 안 좋은 점수
        // 각 테스트 결과를 조회하여 더 심한 조건과 점수로 업데이트
        for ((index,evaluationSituation) in evaluationSituations.withIndex()) {
            // DelistingConditions에 compare 함수를 구현해뒀습니다. DELIST > CARE
            when (evaluationSituation.compare(situation)) {
                // 기존 최악 조건보다 지금 보는 조건이 더 안 좋다면 조건과 최대 점수를 업데이트
                1 -> {
                    situation = evaluationSituation
                    maxScore = evaluationScores[index]
                }
                // 기존 최악 조건과 지금 보는 조건이 같다면 둘 중 더 안 좋은 점수로 업데이트
                0 -> {
                    maxScore = maxOf(evaluationScores[index], maxScore)
                }
            }
        }
        return Pair(situation, maxScore)
    }
    private fun getCorpData() : Pair<DelistingConditions, Double>{
        val retrofitManager = RetrofitManager()
        retrofitManager.searchCorpData(companyName, ::parseRetrofitResponse)
        val investEvaluator = InvestEvaluator(jArray, companyName)

        // 4개 검사 결과를 ArrayList<DelistingConditions>, ArrayList<Double> 형태
        // 4개 테스트 관리/상폐조건 상태 어레이리스트, 4개 테스트 점수 어레이리스트 로 받아서
        // 제일 심한 결과를 출력하는 과정을 짰습니다

        // situation == DelistingConditions.CARE 면 관리조건, DelistingConditions.DELIST 면 상폐조건
        val (evaluationSituations, evaluationScores) = investEvaluator.evaluateCorp()
        var situation = DelistingConditions.CARE // 관리 조건 , 상폐 조건
        var maxScore = 0.0 // 가장 안 좋은 점수
        // 각 테스트 결과를 조회하여 더 심한 조건과 점수로 업데이트
        for ((index,evaluationSituation) in evaluationSituations.withIndex()) {
            // DelistingConditions에 compare 함수를 구현해뒀습니다. DELIST > CARE
            when (evaluationSituation.compare(situation)) {
                // 기존 최악 조건보다 지금 보는 조건이 더 안 좋다면 조건과 최대 점수를 업데이트
                1 -> {
                    situation = evaluationSituation
                    maxScore = evaluationScores[index]
                }
                // 기존 최악 조건과 지금 보는 조건이 같다면 둘 중 더 안 좋은 점수로 업데이트
                0 -> {
                    maxScore = maxOf(evaluationScores[index], maxScore)
                }
            }
        }
        return Pair(situation, maxScore)
    }
}