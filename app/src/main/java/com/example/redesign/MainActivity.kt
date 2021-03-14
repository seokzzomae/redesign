package com.example.redesign

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val TAG = "MainActivity"
        const val NUM_MYUNGUN = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFrag()

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

    private fun initFrag() {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.main_frame, Fragment1())
        ft.add(R.id.main_frame, Fragment2())
        ft.commit()
    }

    private fun setFrag(fragNum: Int) {
        val ft = supportFragmentManager.beginTransaction()

        when(fragNum)
        {
            0 ->{
                ft.hide(Fragment2())
                ft.show(Fragment1())
                ft.commit()
            }
            1 ->{
                ft.hide(Fragment1())
                ft.show(Fragment2())
                ft.commit()
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

    fun getCorpData(corp_name: String) {
        val assetManager: AssetManager = resources.assets
        val inputStream= assetManager.open("jsonfile")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jObject = JSONObject(jsonString)
        val jArray = jObject.getJSONArray("rprts")
        val investEvaluator = InvestEvaluator(jArray, corp_name)
    }
}