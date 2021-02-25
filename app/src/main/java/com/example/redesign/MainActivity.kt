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




        var xml_data =assets.open("file1.xml")
        var factory =XmlPullParserFactory.newInstance()
        var parser =factory.newPullParser()

        var index =Random.nextInt(0,4)


        parser.setInput(xml_data,null)

        var event =parser.eventType
        while (event != XmlPullParser.END_DOCUMENT){
            var tag_name = parser.name

            var myungun=""
            var name=""

            when(event){


                XmlPullParser.END_TAG ->{
                    if(tag_name =="text"){

                        if(parser.getAttributeValue(0) == index.toString()){

                            var myungun ="\n" +parser.getAttributeValue(1)
                            text_myungun.setText(myungun)

                            var name ="\n" +parser.getAttributeValue(2)
                            text_person.setText(name)

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

