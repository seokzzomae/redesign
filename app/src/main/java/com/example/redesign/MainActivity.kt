package com.example.redesign

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag1.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random as Random
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val TAG = "MainActivity"
        const val NUM_MYUNGUN = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)











        setFrag(0)

        btn_analyze.setOnClickListener {
            setFrag(0)


        }

        btn_findlist.setOnClickListener {
            setFrag(1)

        }













        btn_navi.setOnClickListener{
            layout_drawer.openDrawer(GravityCompat.START)

        }
        naviView.setNavigationItemSelectedListener(this)
    }



    private fun setFrag(fragNum: Int) {
        val ft = supportFragmentManager.beginTransaction()
        when(fragNum)
        {
            0 ->{
                ft.replace(R.id.main_frame, Fragment1()).commit()
            }

            1 ->{
                ft.replace(R.id.main_frame, Fragment2()).commit()
            }
        }
    }

    



    override fun onNavigationItemSelected(item: MenuItem): Boolean { //네비게이션 메뉴 아이템 클릭 시 수행
        when (item.itemId)
        {
            R.id.access -> Toast.makeText(applicationContext,"접근성",Toast.LENGTH_SHORT).show()
            R.id.email -> Toast.makeText(applicationContext,"이메일",Toast.LENGTH_SHORT).show()
            R.id.message -> Toast.makeText(applicationContext,"메세지",Toast.LENGTH_SHORT).show()
        }


        layout_drawer.closeDrawers()

        return false
    }


    fun myungun() {
        // TODO!!!!!! getAttributeValue로 현재 인덱스 값을 랜덤 값과 비교하면서 next()로 넘기지 말고
        // 그냥 첫 "text" 태그 나오면 랜덤 값 횟수만큼 next() 해서 명언 찾기로 바꾸는게 연산 숫자 덜 들 것 같습니다.

        val xml_data =assets.open("file1.xml")
        val factory =XmlPullParserFactory.newInstance()
        val parser =factory.newPullParser()

        val index =Random.nextInt(0, NUM_MYUNGUN) // 명언 수가 바뀌면 명언 인덱스 범위가 바뀔테니 변수처리 하였습니다.

        parser.setInput(xml_data,null)

        var event =parser.eventType
        while (event != XmlPullParser.END_DOCUMENT){
            val tag_name = parser.name

            when(event){
                XmlPullParser.END_TAG ->{
                    if(tag_name =="text"){

                        if(parser.getAttributeValue(0) == index.toString()){

                            val myungun ="\n" +parser.getAttributeValue(1)
                            text_myungun.text = myungun

                            val name ="\n" +parser.getAttributeValue(2)
                            text_person.text = name
                        }
                    }
                }
            }

            event = parser.next()

        }
    }


    fun companyinput() {
        val intent =Intent(this,Result::class.java)
        intent.putExtra("company",companyname.text.toString())
        startActivity(intent)
    }
}

