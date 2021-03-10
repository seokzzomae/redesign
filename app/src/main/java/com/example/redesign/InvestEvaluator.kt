package com.example.redesign

import android.util.Log
import com.example.redesign.models.DelistingConditions
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.round
import kotlin.reflect.KFunction


class InvestEvaluator (jArray: JSONArray, private val corpClass: String){

    private val dataStatus : ArrayList<Int> = arrayListOf()
    private val salesAmount : ArrayList<Long?> = arrayListOf()
    private val netIncomeAmount : ArrayList<Long?> = arrayListOf()
    private val ownershipAmount : ArrayList<Long?> = arrayListOf()
    private val businessProfitAmount : ArrayList<Long?> = arrayListOf()
    private val capitalAmount : ArrayList<Long?> = arrayListOf()
    private val totalAssetsAmount : ArrayList<Long?> = arrayListOf()

    init {
        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
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
    }

    private val curYearRprtCnt = dataStatus.slice(0 until 4).sum()  // 올해
    private val lastYearRprtCnt = dataStatus.slice(4 until 8).sum()  // 작년

    // 조건값
    // 항목 1 조건 - 매출액 - 코스피: 50 억 / 코스닥: 30억
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

    fun evaluateCorp() : Pair<String, Double>{
        var maxScore = 0
        var situation : DelistingConditions
        val tests = arrayOf(::testCapitalErosion)
//        val tests : Array<KFunction<*>> = arrayOf(::testCapitalErosion, ::testLongtermProfit, ::testNetIncome, ::testSalesAmount)

        for (test in tests){
            val thisTest : () -> Pair<DelistingConditions, Double> = ::test
        }


        return Pair(situation, maxScore)
    }

    //  올해부터 몇 년 전인가 0          - 1         - 1             - 2             - 2           - 3           - 3            - 4          - 4               - 5
    //  인덱스 번호          0 2 4 6 -> 1 3 5 7 -> 8 10 12 14 -> 9 11 13 15 -> 16 18 20 22 -> 17 19 21 23 -> 24 26 28 30 -> 25 27 29 31 -> 32 34 36 38 -> 33 35 37 39

    //함수 1 : 매출액
    fun testSalesAmount(): Pair<DelistingConditions, Double>? {
        println("1. 매출액 기준 조건 검사")

        // 여태까지 상태 좋던 애들은 관리 들어기 전이니 관리 조건으로
        val salesStatus : DelistingConditions? = when(curYearRprtCnt){
            0-> {
                when (lastYearRprtCnt) {
                    3 -> if ((salesAmount[22]?:throw IllegalArgumentException("-2 year sales amount is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
                    4 -> if ((salesAmount[14]?:throw IllegalArgumentException("-1 year sales amount is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
                    else -> throw IllegalArgumentException("Prev year reports are less than 3.") //상장된지 2년 이하인 기업은 미리 걸러내서 점수를 내지 않음을 표기해야 함
                }
            }
            else -> if((salesAmount[14]?:throw IllegalArgumentException("sales amount for prev year is null")) < salesLimit) DelistingConditions.DELIST  else DelistingConditions.CARE
        }

        val index = arrayOf(0, 2, 4)
        val tempArray : Array<Long?> = arrayOfNulls(3)
        var tempArraySize : Int = 0
        for (i in index){
            salesAmount[i]?.run { // if(salesAmount[i] != null 과 같음
                tempArray[tempArraySize++] = salesAmount[i]
            }
        }

        var salesScore : Double =
                if (tempArraySize != 0) {
            when {
                tempArray.slice(0 until tempArraySize).filterNotNull().sum() >= tempArraySize.toDouble() * salesLimit / 4 -> {
                    when (tempArraySize) {
                        1 -> (1 - (salesAmount[0]?:throw IllegalArgumentException("null in salesAmount[0]")).times(4).toDouble() / salesLimit) * 100
                        2 -> (1 - sumNullable(salesAmount[0], salesAmount[2]?.times(3)).toDouble() / salesLimit) * 100
                        3 -> (1 - sumNullable(salesAmount[0], salesAmount[2], salesAmount[4]).toDouble() / salesLimit) * 100
                        else -> { //나올 일 없음.
                            throw IllegalArgumentException("sales_score: null\ntemp_array_size: $tempArraySize (not in [1,2,3])")
                        }
                    }
                }
                tempArraySize < 3 -> (1 - (salesAmount[0]?:throw IllegalArgumentException("null in salesAmount[0])")).times(4).toDouble() / (tempArraySize.toDouble() * salesLimit / 4)) * 100
                else -> 0.0
            }
        }
        else 0.0

        salesScore = if (salesScore < 0) 0.0 else salesScore
        println("$salesStatus ${round(salesScore*100)/100} %")

        return when (salesStatus) {
            null -> {
                Log.i(TAG, "null report in testSalesAmount()")
                null
            }
            else -> Pair(salesStatus, salesScore)
        }
    }

    //함수 2 : 법인세 비용차감전계속 사업손실
    fun testNetIncome() : Unit {
        println("2. 법인세 비용차감전계속 사업손실 검사")
        val bzReportIndices = arrayOf(38, 30, 22, 14) // 4,3,2,1년 전 사업보고서 인덱스
        val scoreArray : Array<Int> = Array(bzReportIndices.size) {0}
        if (corpClass == "K") {
            for ((score_index, bz_rp_index) in bzReportIndices.withIndex()) {
                val condition = netIncomeAmount[bz_rp_index]?.let {
                    (it < netIncomeLimit) and (abs(it) > ((ownershipAmount[bz_rp_index]?.toDouble()
                            ?: throw IllegalArgumentException("ownershipAmount[${bz_rp_index}] is null")) / 2))
                }
                        ?: throw IllegalArgumentException("netIncomeAmount[${bz_rp_index}] is null")
                if (condition) scoreArray[score_index] = 1 // 2번 조건 만족시 1
            }
            when (curYearRprtCnt) {
                0 -> {
                    when (lastYearRprtCnt) {
                        3 -> {
                            val lastYearNetIncome = sumNullable(netIncomeAmount[8], netIncomeAmount[10], netIncomeAmount[12])
                            val condition = (lastYearNetIncome < (netIncomeLimit.toDouble() * 3 / 4)) and (abs(lastYearNetIncome) > ((ownershipAmount[12]?.toDouble()
                                    ?: throw IllegalArgumentException("ownershipAmount[12] is null")) / 2))
                            when {
                                (scoreArray.sum() == 2) and (scoreArray[2] == 1) -> {
                                    if (condition) {
                                        println("관리 조건 : 100%")
                                        println("상폐 조건 : 87.5%")
                                    } else {
                                        println("관리 조건 : 100%")
                                        println("상폐 조건 : 0%")
                                    }
                                }
                                scoreArray.sum() == 0 -> {
                                    if (condition) {
                                        println("관리 조건 : 37.5%")
                                        println("상폐 조건 : 0%")
                                    } else {
                                        println("관리 조건 : 0%")
                                        println("상폐 조건 : 0%")
                                    }
                                }
                                scoreArray[1] + scoreArray[2] == 1 -> {
                                    if (condition) {
                                        println("관리 조건 : 87.5%")
                                        println("상폐 조건 : 0%")
                                    } else {
                                        println("관리 조건 : 0%")
                                        println("상폐 조건 : 0%")
                                    }
                                }
                                else -> {
                                    if (condition) {
                                        println("관리 조건 : 37.5%")
                                        println("상폐 조건 : 0%")
                                    } else {
                                        println("관리 조건 : 0%")
                                        println("상폐 조건 : 0%")
                                    }
                                }
                            }
                        }
                        4 -> {
                            when {
                                (scoreArray.sum() >= 3) and (scoreArray[2] == 1) -> {
                                    println("관리 조건 : 100%")
                                    println("상폐 조건 : 100%")
                                }
                                (scoreArray.slice(1..scoreArray.lastIndex).sum() >= 2) and (scoreArray[3] == 1) -> {
                                    println("관리 조건 : 100%")
                                    println("상폐 조건 : 0%")
                                }
                                (scoreArray.slice(2..scoreArray.lastIndex).sum() >= 1) and (scoreArray[3] == 1) -> {
                                    println("관리 조건 : 50%")
                                    println("상폐 조건 : 0%")
                                }
                                else -> {
                                    println("관리 조건 : 0%")
                                    println("상폐 조건 : 0%")
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val condition = netIncomeAmount[0]?.let {
                        (it < (netIncomeLimit.toDouble() * 1 / 4)) and (abs(it) > (ownershipAmount[0]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[0] is null")) / 2)
                    }
                            ?: throw IllegalArgumentException("netIncomeAmount[0] is null")
                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            if (condition) {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 25%")
                            } else {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            if (condition) {
                                println("관리 조건 : 12.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            if (condition) {
                                println("관리 조건 : 62.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 50%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        else -> {
                            if (condition) {
                                println("관리 조건 : 12.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                    }
                }
                2 -> {
                    val condition = sumNullable(netIncomeAmount[0], netIncomeAmount[2]).let {
                        (it < (netIncomeLimit.toDouble() * 2 / 4)) and (abs(it) > (ownershipAmount[2]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[2] is null")) / 2)
                    }

                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            if (condition) {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 50%")
                            } else {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            if (condition) {
                                println("관리 조건 : 25%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            if (condition) {
                                println("관리 조건 : 75%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 50%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        else -> {
                            if (condition) {
                                println("관리 조건 : 25%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                    }
                }
                3 -> {
                    val condition = sumNullable(netIncomeAmount[0], netIncomeAmount[2], netIncomeAmount[4]).let {
                        (it < (netIncomeLimit.toDouble() * 3 / 4)) and (abs(it) > (ownershipAmount[4]?.toDouble()
                                ?: throw IllegalArgumentException("ownershipAmount[4] is null")) / 2)
                    }
                    when {
                        (scoreArray.slice(1..scoreArray.lastIndex).sum() == 2) and (scoreArray[3] == 1) -> {
                            if (condition) {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 75%")
                            } else {
                                println("관리 조건 : 100%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.sum() == 0 -> {
                            if (condition) {
                                println("관리 조건 : 37.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        scoreArray.slice(2..scoreArray.lastIndex).sum() == 1 -> {
                            if (condition) {
                                println("관리 조건 : 87.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 50%")
                                println("상폐 조건 : 0%")
                            }
                        }
                        else -> {
                            if (condition) {
                                println("관리 조건 : 37.5%")
                                println("상폐 조건 : 0%")
                            } else {
                                println("관리 조건 : 0%")
                                println("상폐 조건 : 0%")
                            }
                        }
                    }
                }
            }
        }
        else println("코스닥만 해당")
    }

    //함수 3 : 장기간 영업손실
    fun testLongtermProfit(): Unit {
        println("3. 장기간 영업손실 검사")
        if(corpClass == "K"){
            if(curYearRprtCnt == 0) {
                if (lastYearRprtCnt > 3) { // 징검다리로 영업손실이 나는 건 상관 없는 건가요?
                    if ((businessProfitAmount[14]
                                    ?: throw IllegalArgumentException("-1 year businessProfitAmount is null")) < 0) {
                        if ((businessProfitAmount[22]
                                        ?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0) {
                            if ((businessProfitAmount[30]
                                            ?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0) {
                                if ((businessProfitAmount[38]
                                                ?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) println("상폐 조건 충족 시작")
                                else println("관리 조건 75% 충족")
                            } else println("관리 조건 50% 충족")
                        } else println("관리 조건 25% 충족")
                    } else println("해당 사항 없음")
                }
                else {
                    val penalty = sumNullable(businessProfitAmount[8], businessProfitAmount[10], businessProfitAmount[12]) 
                    var percentile = if(penalty < 0) 18 else 0
                    if((businessProfitAmount[22]?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0){
                        if((businessProfitAmount[30]?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0){
                            if((businessProfitAmount[38]?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) {
                                if ((businessProfitAmount[39]
                                                ?: throw IllegalArgumentException("-5 year businessProfitAmount is null")) < 0) {
                                    if (penalty < 0) println("상폐 조건 75% 충족")
                                }
                                else {
                                    percentile += 75
                                    println("관리 조건 $percentile% 충족")
                                }
                            }
                            else {
                                percentile += 50
                                println("관리 조건 $percentile% 충족")
                            }
                        }
                        else {
                            percentile += 25
                            println("관리 조건 $percentile% 충족")
                        }
                    } else println("해당 사항 없음")
                }
            }
            else{
                var penalty : Long = 0L
                for(i in 0 until curYearRprtCnt) penalty += businessProfitAmount[i*2-2]?:throw IllegalArgumentException("businessProfitAmount[${i*2-2}] is null")
                var percentile = if(penalty < 0) 6 * curYearRprtCnt else 0
                if((businessProfitAmount[14]?:throw IllegalArgumentException("-1 year businessProfitAmount is null"))<0) {
                    if ((businessProfitAmount[22]
                                    ?: throw IllegalArgumentException("-2 year businessProfitAmount is null")) < 0) {
                        if ((businessProfitAmount[30]
                                        ?: throw IllegalArgumentException("-3 year businessProfitAmount is null")) < 0) {
                            if ((businessProfitAmount[38]
                                            ?: throw IllegalArgumentException("-4 year businessProfitAmount is null")) < 0) {
                                val sp_score = if (penalty < 0) 25 * curYearRprtCnt else 0
                                println("상폐 조건 ${sp_score}% 충족")
                            } else {
                                percentile += 75
                                println("상폐 조건 ${percentile}% 충족")
                            }
                        } else {
                            percentile += 50
                            println("상폐 조건 ${percentile}% 충족")
                        }
                    } else {
                        percentile += 25
                        println("상폐 조건 ${percentile}% 충족")
                    }
                } else {
                    if (percentile == 0) println("해당 사항 없음")
                    else println("상폐 조건 ${percentile}% 충족")
                }
            }
        }
        else println("코스피는 해당 사항 없음")
    }

    //함수 4: 자본 잠식
    fun testCapitalErosion() : Unit{
        fun capitalErosionRate(index : Int) : Double{
            return capitalAmount[index]?.let{(it-(ownershipAmount[index]?.toDouble()?:throw IllegalArgumentException("ownershipAmount[$index] is null")))/it * 100}
                    ?:throw IllegalArgumentException("capitalAmount[$index] is null")
        }
        when(corpClass){
            "K"->{
                when(curYearRprtCnt) {
                    0 -> when(lastYearRprtCnt){
                             4 -> {
                                if(capitalErosionRate(14)>=capitalErosionRateLimit) println("관리 A 100%") // null checked.
                                else println("관리 A 0%")
                                if(ownershipAmount[14]!! < capitalErosionLimit) println("관리 B 100%")
                                else println("관리 B 0%")
                            }
                            else ->{
                                if(capitalErosionRate(12)>=capitalErosionRateLimit) println("관리 A 50%") // null checked.
                                else println("관리 A 0%")
                                if(ownershipAmount[12]!! < capitalErosionLimit.toDouble()/2) println("관리 B 50%")
                                else println("관리 B 0%")
                            }
                        }
                    1 -> {
                        when{
                            (capitalErosionRate(14)>=capitalErosionRateLimit) and (capitalErosionRate(0)>=capitalErosionRateLimit) -> println("상폐 A 50%")
                            capitalErosionRate(14)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(0)>=capitalErosionRateLimit -> println("관리 A 50%")
                            else -> println("관리 A 0%")

                        }
                        when{
                            (ownershipAmount[14]!! < capitalErosionLimit) and (ownershipAmount[0]!! < capitalErosionLimit.toDouble()/2) -> println("상폐 B 50%")
                            ownershipAmount[14]!! < capitalErosionLimit -> {
                                println("관리 B 100%")
                                println("상폐 B 0%")
                            }
                            ownershipAmount[0]!! < capitalErosionLimit.toDouble()/2 -> println("관리 B 50%")
                            else -> println("관리 B 0%")
                        }
                    }
                    2 -> {
                        when{
                            (capitalErosionRate(14)>=capitalErosionRateLimit) and (capitalErosionRate(2)>=capitalErosionRateLimit) -> println("상폐 A 100%")
                            capitalErosionRate(14)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(2)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            else -> println("관리 A 0%")

                        }
                        when{
                            (ownershipAmount[14]!! < capitalErosionLimit) and (ownershipAmount[2]!! < capitalErosionLimit) -> println("상폐 B 100%")
                            ownershipAmount[14]!! < capitalErosionLimit -> {
                                println("관리 B 100%")
                                println("상폐 B 0%")
                            }
                            ownershipAmount[2]!! < capitalErosionLimit -> println("관리 B 100%")
                            else -> println("관리 B 0%")
                        }

                    }
                    3 -> {
                        when{
                            (capitalErosionRate(2)>=capitalErosionRateLimit) and (capitalErosionRate(4)>=capitalErosionRateLimit) -> println("상폐 A 50%")
                            capitalErosionRate(2)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(4)>=capitalErosionRateLimit -> println("관리 A 50%")
                            else -> println("관리 A 0%")

                        }
                        when{
                            (ownershipAmount[2]!! < capitalErosionLimit) and (ownershipAmount[4]!! < capitalErosionLimit) -> println("상폐 B 50%")
                            ownershipAmount[2]!! < capitalErosionLimit -> {
                                println("관리 B 100%")
                                println("상폐 B 0%")
                            }
                            ownershipAmount[5]!! < capitalErosionLimit.toDouble()/2 -> println("관리 B 50%")
                            else -> println("관리 B 0%")
                        }
                    }
                }
            }
            "Y" ->{
                when(curYearRprtCnt){
                    0->{
                        when(lastYearRprtCnt){
                            4 -> {
                                if(capitalErosionRate(14)>=capitalErosionRateLimit) println("관리 A 100%") // null checked.
                                else println("관리 A 0%")
                            }
                            else -> {
                                if(capitalErosionRate(12)>=capitalErosionRateLimit) println("관리 A 50%") // null checked.
                                else println("관리 A 0%")
                            }
                        }
                    }
                    1->{
                        when{
                            (capitalErosionRate(14)>=capitalErosionRateLimit) and (capitalErosionRate(0)>=capitalErosionRateLimit) -> println("상폐 A 25%")
                            capitalErosionRate(14)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(0)>=50 -> println("관리 A 25%")
                            else -> println("관리 A 0%")
                        }
                    }
                    2->{
                        when{
                            (capitalErosionRate(14)>=capitalErosionRateLimit) and (capitalErosionRate(2)>=capitalErosionRateLimit) -> println("상폐 A 50%")
                            capitalErosionRate(14)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(2)>=capitalErosionRateLimit -> {
                                println("관리 A 50%")
                                println("상폐 A 0%")
                            }
                            else -> println("관리 A 0%")
                        }
                    }
                    3->{
                        when{
                            (capitalErosionRate(14)>=capitalErosionRateLimit) and (capitalErosionRate(4)>=capitalErosionRateLimit) -> println("상폐 A 75%")
                            capitalErosionRate(14)>=capitalErosionRateLimit -> {
                                println("관리 A 100%")
                                println("상폐 A 0%")
                            }
                            capitalErosionRate(4)>=capitalErosionRateLimit -> println("관리 A 75%")
                            else -> println("관리 A 0%")
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Unidentified market label $corpClass")
        }
        println("작년자본잠식률 : ${capitalErosionRate(14)}")
        println("올해 1분기 자본잠식률 : ${capitalErosionRate(0)}")
        println("올해 반기 자본잠식률 : ${capitalErosionRate(2)}")

        //자기자본 = 자산총계
        println("작년 자기자본 : ${totalAssetsAmount[14]}")
        println("올해 1분기 자기자본 : ${totalAssetsAmount[0]}")
        println("올해 자기자본 : ${totalAssetsAmount[2]}")
    }
}


