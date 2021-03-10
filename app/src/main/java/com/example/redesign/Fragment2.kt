package com.example.redesign


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.frag2.*

class Fragment2 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.frag2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView()
    }

    private fun recyclerView() {
        val profileList = arrayListOf(
                profiles( "김석호","관리조건",25),
                profiles( "삼성전자","관리조건",0),
                )

        rv_profile.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false )
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter =ProfileAdapter(profileList)
    }
}