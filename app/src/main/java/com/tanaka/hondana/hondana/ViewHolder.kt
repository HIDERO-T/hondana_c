package com.tanaka.hondana.hondana
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
/**
 * Created by naoi on 2017/04/25.
 */

class CasarealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView
    var authorView: TextView
    var statusButton: Button
    init {
        titleView = itemView.findViewById<View>(R.id.title) as TextView
        authorView = itemView.findViewById<View>(R.id.author) as TextView
        statusButton = itemView.findViewById(R.id.buttonStatus) as Button
    }
}

class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    var titleView: TextView
    var authorView: TextView
    var borrowDay: TextView
    var returnDay: TextView
    init {
        titleView = itemView.findViewById<View>(R.id.title) as TextView
        authorView = itemView.findViewById<View>(R.id.author) as TextView
        borrowDay = itemView.findViewById<View>(R.id.textBorrow) as TextView
        returnDay = itemView.findViewById<View>(R.id.textReturn) as TextView
    }

}