package com.tanaka.hondana.hondana

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup


/**
 * Created by naoi on 2017/04/25.
 */

class CasarealRecycleViewAdapter(private val list: List<BookData>) : RecyclerView.Adapter<CasarealViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CasarealViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return CasarealViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: CasarealViewHolder, position: Int) {
        holder.titleView.text = list[position].title
        holder.authorView.text = list[position].author
        holder.statusButton.text = list[position].status.toString()
        holder.statusButton.isEnabled = list[position].isEnabled
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

class ListRecycleViewAdapter(private val list: List<ListRowData>) : RecyclerView.Adapter<ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return ListViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.titleView.text = list[position].title
        holder.authorView.text = list[position].author
        holder.borrowDay.text = list[position].borrowDay.toString()
        holder.returnDay.text = list[position].returnDay.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}