package com.athena.projectgroupwareapp.main.tab1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.athena.projectgroupwareapp.R
import com.bumptech.glide.Glide

class TotalAdapter constructor(var context: Context, var items : MutableList<TotalItem>) : Adapter<TotalAdapter.VH>() {

    inner class VH(itemView: View) : ViewHolder(itemView){

        val title : TextView by lazy { itemView.findViewById(R.id.tv_total_title) }
        val date : TextView by lazy { itemView.findViewById(R.id.tv_total_date) }
        val icon : ImageView by lazy { itemView.findViewById(R.id.iv_total) }
        val url : WebView by lazy { itemView.findViewById(R.id.webView) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var itemView : View = LayoutInflater.from(context).inflate(R.layout.recycler_item_notification,parent,false)
        return VH(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        //코틀린은 리스트의 get()대신 배열처럼 []를 선호한다
        var item : TotalItem = items[position]
        holder.title.setText(item.title)
        holder.date.setText(item.date)
        Glide.with(context).load(item.icon).into(holder.icon)

        holder.itemView.setOnClickListener {

            val intent : Intent = Intent(context, NotificationActivity::class.java)
            intent.putExtra("title",item.title)
            intent.putExtra("date",item.date)
            intent.putExtra("url",item.url)

            context.startActivity(intent)
        }




    }
}













