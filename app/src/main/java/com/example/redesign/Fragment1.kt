package com.example.redesign


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.frag1.*
import java.nio.channels.AsynchronousServerSocketChannel.open
import java.nio.channels.FileChannel.open
import kotlin.random.Random as Random
import kotlinx.android.synthetic.main.activity_main.*



class Fragment1 : Fragment() {
    private lateinit var mainActivity : MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.frag1, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resultButton.setOnClickListener {
            mainActivity.companyinput()
        }
        mainActivity.myungun()
//        getCorpData 작동하는지 확인하기 위해 추가한 코드. 추후 삭제
//        mainActivity.getCorpData("anything")
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("data1" ,text_myungun.text.toString())
    }
}