package com.example.redesign

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter (val profileList: ArrayList<profiles>) : RecyclerView.Adapter<ProfileAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent,false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    override fun onBindViewHolder(holder: ProfileAdapter.CustomViewHolder, position: Int) {
        holder.name.text= profileList.get(position).name
        holder.jogun.text = profileList.get(position).jogun
        holder.score.text = profileList.get(position).score.toString()
    }
    
    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.tv_name)
        val jogun = itemView.findViewById<TextView>(R.id.tv_jogun)
        val score = itemView.findViewById<TextView>(R.id.tv_score)
    }
}