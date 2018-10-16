package com.tanaka.hondana.hondana

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast


class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<Button>(R.id.buttonSearch).setOnClickListener{
            findViewById<ProgressBar>(R.id.progressBar3).visibility = View.VISIBLE
            val rv = findViewById<RecyclerView>(R.id.recyclerView) as RecyclerView
            val adapter = CasarealRecycleViewAdapter(this.createDataset())
            val llm = LinearLayoutManager(this)
            rv.setHasFixedSize(true)
            rv.layoutManager = llm
            rv.adapter = adapter
            findViewById<ProgressBar>(R.id.progressBar3).visibility = View.INVISIBLE
        }

    }

    private fun createDataset(): List<RowData> {
        //TODO: API接続、JSON取得してarrayListOf<RowData>を作成。
        val dataset = arrayListOf<RowData>()
        for (i in 0..49) {
            val data = RowData()
            data.title = "Ruby公式資格教科書 : Ruby技術者認定試験Silver/Gold対応 : A Programmer's Best Friend"
            data.author = "増井雄一郎, 小川伸一郎, 藁谷修一, 川尻剛, 牧俊男 著,Rubyアソシエーション,CTCテクノロジー(株) 監修,"
            data.status = i % 4
            dataset.add(data)
        }
        return dataset
    }


}
