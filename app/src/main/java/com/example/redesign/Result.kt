package com.example.redesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.frag1.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random
import kotlin.math.min as main

class Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)



        val company1 = intent.getStringExtra("company")   //회사 이름 받음

        resultname.setText(company1)



        var situation="관리 조건"  //관리 조건 , 상폐 조건

        var jogun1=""



        var Max_score=25  //가장 안 좋은 점수


        var text1 ="관련 설명 쓰는 곳곳!!!"


        score.progress=Max_score




        detail_opinion.setText(text1)

        back.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)

        }



        when{
            Max_score >= 75 ->
                resultImage.setBackgroundResource(
                        R.drawable.bad_result
                )


            Max_score > 25 ->
                resultImage.setBackgroundResource(
                        R.drawable.medium_result
                )

            Max_score >= 0 ->
                resultImage.setBackgroundResource(
                        R.drawable.good_result
                )

        }

        when{
            Max_score >= 75 ->
                yuhumdo.setText("매우 위험")


            Max_score > 25 ->
                yuhumdo.setText("위험")

            Max_score >= 0 ->
                yuhumdo.setText("양호")

        }


        when{
            Max_score >= 75 ->
                opinion.setText("투자를 삼가하세요!")


            Max_score > 25 ->
                opinion.setText("자산이 위험해요!")

            Max_score >= 0 ->
                opinion.setText(situation+"은 안 될것 같아요!")

        }










    }
}


