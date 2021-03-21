package com.example.redesign

import android.util.Log
import com.example.redesign.models.DelistingConditions
import org.json.JSONArray
import java.util.function.DoubleBinaryOperator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

class InvestEvaluator (jArray: JSONArray, private val corpClass: String){
    private val dataStatus : ArrayList<Int> = arrayListOf()
    private val salesAmount : ArrayList<Long?> = arrayListOf()
    private val netIncomeAmount : ArrayList<Long?> = arrayListOf()
    private val ownershipAmount : ArrayList<Long?> = arrayListOf()
    private val businessProfitAmount : ArrayList<Long?> = arrayListOf()
    private val capitalAmount : ArrayList<Long?> = arrayListOf()
    private val totalAssetsAmount : ArrayList<Long?> = arrayListOf()
    private val lastYearRprtCnt: Int
    private val curYearRprtCnt: Int

    // 1분기보고서 : 11013
    // 반기보고서 : 11012
    // 3분기보고서 : 11014
    // 사업보고서 : 11011

    init {
        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
            // TODO null 로 들어온 json 데이터를 kotlin null로 바꿔줘야 함.
            dataStatus.add(obj.getString("data_status").toInt())
            salesAmount.add(obj.getString("sales_amount").toLong())
            netIncomeAmount.add(obj.getString("net_income_amount").toLong())
            ownershipAmount.add(obj.getString("ownership_amount").toLong())
            businessProfitAmount.add(obj.getString("business_profit_amount").toLong())
            capitalAmount.add(obj.getString("capital_amount").toLong())
            totalAssetsAmount.add(obj.getString("total_assets_amount").toLong())
        }
        Log.i(TAG, "dataStatus: ${dataStatus}")
        Log.i(TAG, "salesAmount: ${salesAmount}")
        Log.i(TAG, "netIncomeAmount: ${netIncomeAmount}")
        Log.i(TAG, "ownershipAmount: ${ownershipAmount}")
        Log.i(TAG, "businessProfitAmount: ${businessProfitAmount}")
        Log.i(TAG, "capitalAmount: ${capitalAmount}")
        Log.i(TAG, "totalAssetsAmount: ${totalAssetsAmount}")
        var cnt = 0
        for (i in dataStatus.slice(0 until 4)) {
            if (i == 0)
                cnt += 1 // 올해
        }
        curYearRprtCnt = cnt

        cnt = 0
        for (i in dataStatus.slice(4 until 8)) {
            if (i == 0)
                cnt += 1 // 작년
        }
        lastYearRprtCnt = cnt
    }


//    private val curYearRprtCnt = {
//        var cnt = 0
//        for (i in dataStatus.slice(0 until 4)) {
//            if (i == 13)
//                cnt += 1 // 올해
//        }
//        cnt
//    }
//    private val lastYearRprtCnt = {
//        var cnt = 0
//        for (i in dataStatus.slice(4 until 8)) {
//            if (i == 13)
//                cnt += 1 // 작년
//        }
//        cnt
//    }


    // 조건값
    // 항목 1 조건 - 매출액 - 코스피: 50 억 / 코스닥: 30억 2년 연속시 상폐
    private val salesLimit = if (corpClass == "Y") 5000000000 else 3000000000

    // 모든 InvestEvaluator 클래스 오브젝트에 동일하게 적용되는 변수(자바에선 static 변수라고 불리는 변수)
    // 참고 - https://medium.com/@lunay0ung/kotlin-object-declaration-%EA%B7%B8%EB%A6%AC%EA%B3%A0-companion-object-feat-static-d5c97c21168
    companion object{
        const val TAG = "InvestEvaluator"
        // 항목 2 조건 - 법인세 비용차감전계속 사업손실: -10억
        const val netIncomeLimit = -1000000000
        // 항목 4 조건 - 자본잠식: 10억, 잠식률: 50%
        const val capitalErosionLimit = 1000000000
        const val capitalErosionRateLimit = 50
    }

    // null 가능한 변수들의 합을 구하는 함수
    private fun sumNullable(vararg longs: Long?) : Long{
        var sum = 0L
        for (long in longs) {
            sum += (long?:throw IllegalArgumentException("values for sum contain null"))
        }
        return sum
    }

    fun evaluateCorp() : Pair<ArrayList<DelistingConditions>, ArrayList<Double>>{
//        val tests = arrayOf(::testCapitalErosion)
//        val tests : Array<KFunction<*>> = arrayOf(::testCapitalErosion, ::testLongtermProfit, ::testNetIncome, ::testSalesAmount)
//
//        for (test in tests){
//            val thisTest : () -> Pair<DelistingConditions, Double>= ::test
//        }
        val (salesAmountSituation, salesAmountScore) = testSalesAmount()
        val (netIncomeSituation , netIncomeScore) = testNetIncome()
        val (longtermProfitSituation, longtermProfitScore) = testLongtermProfit()
        val (capitalErosionSituation, capitalErosionScore) = testCapitalErosion()

//        return Pair(arrayListOf(salesAmountSituation, netIncomeSituation, capitalErosionSituation), arrayListOf(salesAmountScore, netIncomeScore, capitalErosionScore))
        return Pair(arrayListOf(salesAmountSituation, netIncomeSituation , longtermProfitSituation, capitalErosionSituation), arrayListOf(salesAmountScore, netIncomeScore, longtermProfitScore, capitalErosionScore))
    }

    //  올해부터 몇 년 전인가 0          - 1         - 1             - 2             - 2           - 3           - 3            - 4          - 4               - 5
    //  인덱스 번호          0 2 4 6 -> 1 3 5 7 -> 8 10 12 14 -> 9 11 13 15 -> 16 18 20 22 -> 17 19 21 23 -> 24 26 28 30 -> 25 27 29 31 -> 32 34 36 38 -> 33 35 37 39

    //  올해부터 몇 년 전인가 0        - 1        - 2           - 3           - 4           - 5
    //                   4 3 2 1 -> 8 7 6 5 -> 12 11 10 9 -> 16 15 14 13 -> 20 19 18 17

    //함수 1 : 매출액
    //항목 1 조건 - 매출액 - 코스피: 50 억 / 코스닥: 30억 2년 연속시 상폐
    private fun testSalesAmount(): Pair<DelistingConditions, Double> {
        Log.i(TAG, "1. 매출액 기준 조건 검사")

        // 여태까지 상태 좋던 애들은 관리 들어기 전이니 관리 조건으로
        val delistingCondition : DelistingConditions = when(curYearRprtCnt){
            0-> {
                when (lastYearRprtCnt) {
                    3 -> if ((salesAmount[9]?:throw IllegalArgumentException("-2 year sales amount is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
                    4 -> if ((salesAmount[5]?:throw IllegalArgumentException("-1 year sales amount is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
                    else -> throw IllegalArgumentException("Prev year reports are less than 3.") // 상장된지 2년 이하인 기업은 미리 걸러내서 점수를 내지 않아야함. 여기서 Null case 방법
                }
            }
            else -> if((salesAmount[5]?:throw IllegalArgumentException("sales amount for prev year is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
        }

        val index = arrayOf(4, 3, 2) // 올해의 1,2,3분기 보고서 인덱스
        val tempArray : Array<Long?> = arrayOfNulls(3)
        var tempArraySize : Int = 0
        for (i in index){
            salesAmount[i]?.run { // if(salesAmount[i] != null 과 같음
                tempArray[tempArraySize++] = salesAmount[i]
            }
        }

        var salesScore : Double = if (tempArraySize != 0) {
            when {
                tempArray.slice(0 until tempArraySize).filterNotNull().sum() >= tempArraySize.toDouble() * salesLimit / 4 -> {
                    when (tempArraySize) {
                        1 -> (1 - (salesAmount[4]?:throw IllegalArgumentException("null in salesAmount[0]")).times(4).toDouble() / salesLimit) * 100
                        2 -> (1 - sumNullable(salesAmount[4], salesAmount[3]?.times(3)).toDouble() / salesLimit) * 100
                        3 -> (1 - sumNullable(salesAmount[4], salesAmount[3], salesAmount[2]).toDouble() / salesLimit) * 100
                        else -> { // 나올 일 없음
                            throw IllegalArgumentException("sales_score: null\ntemp_array_size: $tempArraySize (not in [1,2,3])")
                        }
                    }
                }
                tempArraySize < 3 -> (1 - (salesAmount[4]?:throw IllegalArgumentException("null in salesAmount[0])")).times(4).toDouble() / (tempArraySize.toDouble() * salesLimit / 4)) * 100
                else -> 0.0
            }
        }
        else 0.0

        salesScore = if (salesScore < 0) 0.0 else salesScore
        Log.i(TAG, "$delistingCondition ${round(salesScore*100)/100} %")

        return Pair(delistingCondition, salesScore)
    }

    //함수 2 : 법인세 비용차감전계속 사업손실
    //자기자본의 50%를 초과(&10억원 이상)하는 법인세비용차감전계속사업손실이 최근 3년간 2회 이상 되면 관리, 관리 지정된 뒤 재발하면 상폐
    private fun testNetIncome() : Pair<DelistingConditions, Double> {
        Log.i(TAG, "2. 법인세 비용차감전계속 사업손실 검사")
        val bzReportIndices = arrayOf(17, 13, 9, 5) // 4,3,2,1년 전 사업보고서 인덱스
        val scoreArray : Array<Int> = Array(bzReportIndices.size) {0}
        if (corpClass == "K") {
            for ((score_index, bz_rp_index) in bzReportIndices.withIndex()) {
                val delistingCondition = netIncomeAmount[bz_rp_index]?.let {
                    (it < netIncomeLimit) and (abs(it) > ((ownershipAmount[bz_rp_index]?.toDouble()
                            ?: throw IllegalArgumentException("ownershipAmount[${bz_rp_index}] is null")) / 2))
                }
                        ?: throw IllegalArgumentException("netIncomeAmount[${bz_rp_index}] is null")
                if (delistingCondition) scoreArray[score_index] = 1 // 2번 조건 만족시 1
            }
            when (curYearRprtCnt) {
                0 -> {
                    when (lastYearRprtCnt) {
                        3 -> {
                            val lastYearNetIncome = sumNullable(netIncomeAmount[8], netIncomeAmount[7], netIncomeAmount[6])
                            val lastYearNetIncomeCondition = (lastYearNetIncome < (netIncomeLimit.toDouble() * 3 / 4)) and (abs(lastYearNetIncome) > ((ownershipAmount[6]?.toDouble()
                                    ?: throw IllegalArgumentException("ownershipAmount[12] is null")) / 2))
                            when {
                                (scoreArray.sum() == 2) and (scoreArray[2] == 1) -> {
                                    return if (lastYearNetIncomeCondition) {
                                        Log.i(TAG, "관리 조건 : 100%")
                                        Log.i(TAG, "상폐 조건 : 87.5%")
                                        Pair(DelistingConditions.DELIST, 87.5)
                                    } else {
                                        Log.i(TAG, "관리 조건 : 100%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 100.0)
                                    }
                                }
                                scoreArray.sum() == 0 -> {
                                    return if (lastYearNetIncomeCondition) {
                                        Log.i(TAG, "관리 조건 : 37.5%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 37.5)
                                    } else {
                                        Log.i(TAG, "관리 조건 : 0%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 0.0)
                                    }
                                }
                                scoreArray[1] + scoreArray[2] == 1 -> {
                                    return if (lastYearNetIncomeCondition) {
                                        Log.i(TAG, "관리 조건 : 87.5%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 87.5)
                                    } else {
                                        Log.i(TAG, "관리 조건 : 0%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 0.0)
                                    }
                                }
                                else -> {
                                    return if (lastYearNetIncomeCondition) {
                                        Log.i(TAG, "관리 조건 : 37.5%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 37.5)
                                    } else {
                                        Log.i(TAG, "관리 조건 : 0%")
                                        Log.i(TAG, "상폐 조건 : 0%")
                                        Pair(DelistingConditions.CARE, 0.0)
                                    }
                                }
                            }
                        }
                        4 -> {
                            when {
                                (scoreArray.sum() >= 3) and (scoreArray[2] == 1) -> {
                                    Log.i(TAG, "관리 조건 : 100%")
                                    Log.i(TAG, "상폐 조건 : 100%")
                                    return Pair(DelistingConditions.DELIST, 100.0)
                                }
                                (scoreArray.slice(1..scoreArray.lastIndex).sum() >= 2) and (scoreArray[3] == 1) -> {
                                    Log.i(TAG, "관리 조건 : 100%")
                                    Log.i(TAG, "상폐 조건 : 0%")
                                    return Pair(DelistingConditions.CARE, 100.0)
                                }
                                (scoreArray.slice(2..scoreArray.lastIndex).sum() >= 1) and (scoreArray[3] == 1) -> {
                                    Log.i(TAG, "관리 조건 : 50%")
                                    Log.i(TAG, "상폐 조건 : 0%")
                                    return Pair(DelistingConditions.CARE, 50.0)
                                }
                                else -> {
                                    Log.i(TAG, "관리 조건 : 0%")
                                    Log.i(TAG, "상폐 조건 : 0%")
                                    return Pair(DelistingConditions.CARE, 0.0)
                                }
                            }
                        }
                        else -> {
                            Log.i(TAG, "작년 보고서 수 : ${lastYearRprtCnt} 가 3,혹은 4에 해당하지 않음")
                            throw IllegalArgumentException("작년 보고서 수 : ${lastYearRprtCnt} 가 3,혹은 4에 해당하지 않음")
                        }
                    }
                }
                1 -> {
                    val condition = netIncomeAmount[4]?.let {
                        (it < (netIncomeLimit.toDouble() * 1 / 4)) and (abs(it) > (ownershipAmount[4]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[0] is null")) / 2)
                    }
                            ?: throw IllegalArgumentException("netIncomeAmount[0] is null")
                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 25%")
                                Pair(DelistingConditions.DELIST, 25.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 100.0)
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 12.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 12.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 62.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 62.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 50%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 50.0)
                            }
                        }
                        else -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 12.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 12.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                    }
                }
                2 -> {
                    val condition = sumNullable(netIncomeAmount[4], netIncomeAmount[3]).let {
                        (it < (netIncomeLimit.toDouble() * 2 / 4)) and (abs(it) > (ownershipAmount[3]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[2] is null")) / 2)
                    }

                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 50%")
                                Pair(DelistingConditions.DELIST, 50.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 100.0)
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 25%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 25.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 75%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 75.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 50%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 50.0)
                            }
                        }
                        else -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 25%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 25.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                    }
                }
                3 -> {
                    val condition = sumNullable(netIncomeAmount[4], netIncomeAmount[3], netIncomeAmount[2]).let {
                        (it < (netIncomeLimit.toDouble() * 3 / 4)) and (abs(it) > (ownershipAmount[2]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[4] is null")) / 2)
                    }
                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 75%")
                                Pair(DelistingConditions.DELIST, 75.0)
                            } else {
                                Log.i(TAG, "관리 조건 : 100%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 100.0)
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 37.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 37.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 87.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 87.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 50%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 50.0)
                            }
                        }
                        else -> {
                            return if (condition) {
                                Log.i(TAG, "관리 조건 : 37.5%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 37.5)
                            } else {
                                Log.i(TAG, "관리 조건 : 0%")
                                Log.i(TAG, "상폐 조건 : 0%")
                                Pair(DelistingConditions.CARE, 0.0)
                            }
                        }
                    }
                }
                else -> {
                    Log.i(TAG, "현재 년도 리포트 수 : $curYearRprtCnt 이상.")
                    throw throw IllegalArgumentException("현재 년도 리포트 수 : $curYearRprtCnt 이상.")
                }
            }
        }
        else {
            Log.i(TAG, "Corp class not KOSDAQ")
            // TODO 일단 KOSDAQ 아닌 주식 들어오면 에러 띄우게 해놨는데 코스닥 아닌 애면 그냥 함수 결과를 리턴하지 않는 걸로 해야하나
            throw IllegalArgumentException("Corp Class is not K, Only use KOSDAQ")
        }
    }

    //함수 3 : 장기간 영업손실
    //4 영업연도 영업손실시 관리, 5년 연속시 상폐
    private fun testLongtermProfit(): Pair<DelistingConditions, Double> {
        Log.i(TAG, "3. 장기간 영업손실 검사")

        if(corpClass == "K"){
            if(curYearRprtCnt == 0) {
                if (lastYearRprtCnt > 3) { // 징검다리로 영업손실이 나는 건 상관 없는 건가요?
                    val percentile : Double
                    if ((businessProfitAmount[5]
                                    ?: throw IllegalArgumentException("-1 year businessProfitAmount is null")) < 0) {
                        if ((businessProfitAmount[9]
                                        ?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0) {
                            if ((businessProfitAmount[13]
                                            ?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0) {
                                if ((businessProfitAmount[17]
                                                ?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) {
                                    Log.i(TAG, "상폐 조건 충족 시작")
                                    // TODO: 이거 맞나 체크
                                    return Pair(DelistingConditions.DELIST, 100.0)
                                }
                                else {
                                    Log.i(TAG, "관리 조건 75% 충족")
                                    percentile = 75.0
                                }
                            } else {
                                Log.i(TAG, "관리 조건 50% 충족")
                                percentile = 50.0
                            }
                        } else {
                            Log.i(TAG, "관리 조건 25% 충족")
                            percentile = 25.0
                        }
                    } else {
                        Log.i(TAG, "해당 사항 없음")
                        percentile = 0.0
                    }
                    return Pair(DelistingConditions.CARE, percentile)
                }
                else {
                    val penalty = sumNullable(businessProfitAmount[8], businessProfitAmount[7], businessProfitAmount[6])
                    var percentile = if(penalty < 0) 18 else 0
                    if((businessProfitAmount[9]?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0){
                        if((businessProfitAmount[13]?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0){
                            if((businessProfitAmount[17]?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) {
                                // TODO 준 데이터에는 21~24까지가 (5년 전 데이터가) 없다.
                                if ((businessProfitAmount[21]
                                                ?: throw IllegalArgumentException("-5 year businessProfitAmount is null")) < 0) {
                                    if (penalty < 0) {
                                        Log.i(TAG, "상폐 조건 75% 충족")
                                        return Pair(DelistingConditions.DELIST, 75.0)
                                    }
                                }
                                else {
                                    percentile += 75
                                    Log.i(TAG, "관리 조건 $percentile% 충족")
                                }
                            }
                            else {
                                percentile += 50
                                Log.i(TAG, "관리 조건 $percentile% 충족")
                            }
                        }
                        else {
                            percentile += 25
                            Log.i(TAG, "관리 조건 $percentile% 충족")
                        }
                    } else {
                        Log.i(TAG, "해당 사항 없음")
                        return Pair(DelistingConditions.CARE, 0.0)
                    }
                    return Pair(DelistingConditions.CARE, percentile.toDouble())
                }
            }
            else{
                var penalty : Long = 0L
                for(i in 0 until curYearRprtCnt) penalty += businessProfitAmount[4-i]?:throw IllegalArgumentException("businessProfitAmount[${4-i}] is null")
                var percentile = if(penalty < 0) 6 * curYearRprtCnt else 0
                if((businessProfitAmount[5]?:throw IllegalArgumentException("-1 year businessProfitAmount is null"))<0) {
                    if ((businessProfitAmount[9]
                                    ?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0) {
                        if ((businessProfitAmount[13]
                                        ?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0) {
                            if ((businessProfitAmount[17]
                                            ?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) {
                                percentile = if (penalty < 0) 25 * curYearRprtCnt else 0
                            } else percentile += 75
                        } else percentile += 50
                    } else percentile += 25
                }
                Log.i(TAG, "상폐 조건 ${percentile}% 충족")
                return Pair(DelistingConditions.DELIST, percentile.toDouble())
            }
        }
        else {
            Log.i(TAG, "코스피는 해당 사항 없음")
            // TODO: 코스피일 때 null 값인데 어떻게 할지 고름
            throw IllegalArgumentException("코스피는 해당 사항 없음")
        }
    }

    //함수 4: 자본 잠식
    // [관리]
    // (A) 사업연도(반기)말 자본잠식률 50%이상​
    // (B) 사업연도(반기)말 자기자본 10억 원 미만
    // [상폐]
    // A or C 후 사업연도(반기)말 자본잠식률 50% 이상​
    // B or C 후 사업연도(반기)말 자기자본 10억 원 미만​
    // 최근년말 완전자본잠식
    private fun testCapitalErosion() : Pair<DelistingConditions, Double> {
        fun capitalErosionRate(index : Int) : Double {
            return capitalAmount[index]?.let{(it-(ownershipAmount[index]?.toDouble()?:throw IllegalArgumentException("ownershipAmount[$index] is null")))/it * 100}
                    ?:throw IllegalArgumentException("capitalAmount[$index] is null")
        }
        var criteriaADL = DelistingConditions.CARE
        var criteriaAScore = 0.0
        var criteriaBDL = DelistingConditions.CARE
        var criteriaBScore = 0.0
        val criteria : DelistingConditions
        val score : Double

        when(corpClass){
            "K"->{
                when(curYearRprtCnt) {
                    0 -> when(lastYearRprtCnt){
                             4 -> {
                                if (capitalErosionRate(5)>=capitalErosionRateLimit) {
                                    Log.i(TAG, "관리 A 100%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 100.0
                                }
                                else {
                                    Log.i(TAG, "관리 A 0%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 0.0
                                }
                                if (ownershipAmount[5]!! < capitalErosionLimit) {
                                    Log.i(TAG, "관리 B 100%")
                                    criteriaBDL = DelistingConditions.CARE
                                    criteriaBScore = 100.0
                                }
                                else {
                                    Log.i(TAG, "관리 B 0%")
                                    criteriaBDL = DelistingConditions.CARE
                                    criteriaBScore = 0.0
                                }
                            }
                            else ->{
                                if(capitalErosionRate(6)>=capitalErosionRateLimit) {
                                    Log.i(TAG, "관리 A 50%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 50.0
                                }
                                else {
                                    Log.i(TAG, "관리 A 0%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 0.0
                                }
                                if(ownershipAmount[6]!! < capitalErosionLimit.toDouble()/2) {
                                    Log.i(TAG, "관리 B 50%")
                                    criteriaBDL = DelistingConditions.CARE
                                    criteriaBScore = 50.0
                                }
                                else {
                                    Log.i(TAG, "관리 B 0%")
                                    criteriaBDL = DelistingConditions.CARE
                                    criteriaBScore = 0.0
                                }
                            }
                        }
                    1 -> {
                        when{
                            (capitalErosionRate(5)>=capitalErosionRateLimit) and (capitalErosionRate(0)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 50%")
                                criteriaADL = DelistingConditions.DELIST
                                criteriaAScore = 50.0
                            }
                            capitalErosionRate(5)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(4)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 50%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 50.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }
                        }
                        when{
                            (ownershipAmount[5]!! < capitalErosionLimit) and (ownershipAmount[4]!! < capitalErosionLimit.toDouble()/2) ->{
                                Log.i(TAG, "상폐 B 50%")
                                criteriaBDL = DelistingConditions.DELIST
                                criteriaBScore = 50.0
                            }
                            ownershipAmount[5]!! < capitalErosionLimit -> {
                                Log.i(TAG, "관리 B 100%")
                                Log.i(TAG, "상폐 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 100.0
                            }
                            ownershipAmount[4]!! < capitalErosionLimit.toDouble()/2 -> {
                                Log.i(TAG, "관리 B 50%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 50.0
                            }
                            else -> {
                                Log.i(TAG, "관리 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 0.0
                            }
                        }
                    }
                    2 -> {
                        when{
                            (capitalErosionRate(5)>=capitalErosionRateLimit) and (capitalErosionRate(3)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 100%")
                                criteriaADL = DelistingConditions.DELIST
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(5)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(3)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }
                        }
                        when{
                            (ownershipAmount[5]!! < capitalErosionLimit) and (ownershipAmount[3]!! < capitalErosionLimit) -> {
                                Log.i(TAG, "상폐 B 100%")
                                criteriaBDL = DelistingConditions.DELIST
                                criteriaBScore = 100.0
                            }
                            ownershipAmount[5]!! < capitalErosionLimit -> {
                                Log.i(TAG, "관리 B 100%")
                                Log.i(TAG, "상폐 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 100.0
                            }
                            ownershipAmount[3]!! < capitalErosionLimit -> {
                                Log.i(TAG, "관리 B 100%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 100.0
                            }
                            else -> {
                                Log.i(TAG, "관리 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 0.0
                            }
                        }
                    }
                    3 -> {
                        when{
                            (capitalErosionRate(3)>=capitalErosionRateLimit) and (capitalErosionRate(2)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 50%")
                                criteriaADL = DelistingConditions.DELIST
                                criteriaAScore = 50.0
                            }
                            capitalErosionRate(3)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(2)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 50%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 50.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }

                        }
                        when{
                            (ownershipAmount[3]!! < capitalErosionLimit) and (ownershipAmount[2]!! < capitalErosionLimit) -> {
                                Log.i(TAG, "상폐 B 50%")
                                criteriaBDL = DelistingConditions.DELIST
                                criteriaBScore = 50.0
                            }
                            ownershipAmount[3]!! < capitalErosionLimit -> {
                                Log.i(TAG, "관리 B 100%")
                                Log.i(TAG, "상폐 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 100.0
                            }
                            ownershipAmount[6]!! < capitalErosionLimit.toDouble()/2 -> {
                                Log.i(TAG, "관리 B 50%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 50.0
                            }
                            else -> {
                                Log.i(TAG, "관리 B 0%")
                                criteriaBDL = DelistingConditions.CARE
                                criteriaBScore = 0.0
                            }
                        }
                    }
                    else -> {
                        Log.i(TAG, "당해년도 보고서 수가 0~3에 해당하지 않음")
                        throw IllegalArgumentException("curYearRprtCnt : ${curYearRprtCnt}")
                    }
                }
            }
            "Y" ->{
                when(curYearRprtCnt){
                    0->{
                        when(lastYearRprtCnt){
                            4 -> {
                                if(capitalErosionRate(5)>=capitalErosionRateLimit) {
                                    Log.i(TAG, "관리 A 100%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 100.0
                                }
                                else {
                                    Log.i(TAG, "관리 A 0%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 0.0
                                }
                            }
                            else -> {
                                if(capitalErosionRate(6)>=capitalErosionRateLimit) {
                                    Log.i(TAG, "관리 A 50%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 50.0
                                }
                                else {
                                    Log.i(TAG, "관리 A 0%")
                                    criteriaADL = DelistingConditions.CARE
                                    criteriaAScore = 0.0
                                }
                            }
                        }
                    }
                    1->{
                        when{
                            (capitalErosionRate(5)>=capitalErosionRateLimit) and (capitalErosionRate(4)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 25%")
                                criteriaADL = DelistingConditions.DELIST
                                criteriaAScore = 25.0
                            }
                            capitalErosionRate(5)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(4)>=50 -> {
                                Log.i(TAG, "관리 A 25%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 25.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }
                        }
                    }
                    2->{
                        when{
                            (capitalErosionRate(5)>=capitalErosionRateLimit) and (capitalErosionRate(3)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 50%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 50.0
                            }
                            capitalErosionRate(5)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(3)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 50%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 50.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }
                        }
                    }
                    3->{
                        when{
                            (capitalErosionRate(5)>=capitalErosionRateLimit) and (capitalErosionRate(2)>=capitalErosionRateLimit) -> {
                                Log.i(TAG, "상폐 A 75%")
                                criteriaADL = DelistingConditions.DELIST
                                criteriaAScore = 75.0
                            }
                            capitalErosionRate(5)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 100%")
                                Log.i(TAG, "상폐 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 100.0
                            }
                            capitalErosionRate(2)>=capitalErosionRateLimit -> {
                                Log.i(TAG, "관리 A 75%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 75.0
                            }
                            else -> {
                                Log.i(TAG, "관리 A 0%")
                                criteriaADL = DelistingConditions.CARE
                                criteriaAScore = 0.0
                            }
                        }
                    }
                    else -> {
                        Log.i(TAG, "당해년도 보고서 수가 0~3에 해당하지 않음")
                        throw IllegalArgumentException("curYearRprtCnt : ${curYearRprtCnt}")
                    }
                }
            }
            else -> throw IllegalArgumentException("Unidentified market label $corpClass")
        }
        Log.i(TAG, "작년자본잠식률 : ${capitalErosionRate(5)}")
        Log.i(TAG, "올해 1분기 자본잠식률 : ${capitalErosionRate(4)}")
        Log.i(TAG, "올해 반기 자본잠식률 : ${capitalErosionRate(3)}")

        //자기자본 = 자산총계
        Log.i(TAG, "작년 자기자본 : ${totalAssetsAmount[5]}")
        Log.i(TAG, "올해 1분기 자기자본 : ${totalAssetsAmount[4]}")
        Log.i(TAG, "올해 자기자본 : ${totalAssetsAmount[3]}")

        when {
            (criteriaADL == DelistingConditions.DELIST) and (criteriaBDL == DelistingConditions.DELIST) -> {
                criteria = DelistingConditions.DELIST
                score = max(criteriaAScore, criteriaBScore)
            }
            criteriaADL == DelistingConditions.DELIST -> {
                criteria = DelistingConditions.DELIST
                score = criteriaAScore
            }
            criteriaBDL == DelistingConditions.DELIST -> {
                criteria = DelistingConditions.DELIST
                score = criteriaBScore
            }
            else -> {
                criteria = DelistingConditions.CARE
                score = max(criteriaAScore, criteriaBScore)
            }
        }
        return Pair(criteria, score)
    }
}


