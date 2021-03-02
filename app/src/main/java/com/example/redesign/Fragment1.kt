package com.example.redesign


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor.open
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.frag1.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.nio.channels.AsynchronousFileChannel.open
import java.nio.channels.AsynchronousServerSocketChannel.open
import java.nio.channels.FileChannel.open
import kotlin.random.Random as Random
import kotlinx.android.synthetic.main.activity_main.*



class Fragment1 : Fragment() {


    var mainActivity :MainActivity? =null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity =context as MainActivity

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view =inflater.inflate(R.layout.frag1, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultButton.setOnClickListener {
            mainActivity?.companyinput()
        }
        mainActivity?.myungun()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState?.putString("data1" ,text_myungun.text.toString())
    }
}