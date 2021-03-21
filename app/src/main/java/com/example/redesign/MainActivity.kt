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
            0 -> {
                ft.replace(R.id.main_frame, Fragment1()).commit()
            }

            1 -> {
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


}