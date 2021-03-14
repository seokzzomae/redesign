package com.example.redesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.frag1.*
import kotlin.math.min as main

class Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val companyName = intent.getStringExtra("company")   //회사 이름 받음

        resultname.text = companyName

        var situation = "관리 조건"  //관리 조건 , 상폐 조건

        var maxScore = 25  //가장 안 좋은 점수

        var text1 ="관련 설명 쓰는 곳곳!!!"

        score.progress = maxScore

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
                val text = situation + "은 안 될것 같아요!"
                opinion.text = text
            }
        }
    }
}