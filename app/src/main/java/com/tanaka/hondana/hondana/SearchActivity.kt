package com.tanaka.hondana.hondana

import android.content.DialogInterface
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val searchAsync = SearchAsync()
        searchAsync.setOnCallBack(object : SearchAsync.CallBackTask() {
            override fun callBack(result: String?) {
                super.callBack(result)
                if (result == null) {
                    finishWithError()
                } else {
                    //FIXME: 正常に動かない。
                    //Toast.makeText(this, "処理が正常終了しました。", Toast.LENGTH_SHORT).show()
                    applyData(result)
                }
            }
        })

        findViewById<Button>(R.id.buttonSearch).setOnClickListener {
            findViewById<ProgressBar>(R.id.progressBar3).visibility = View.VISIBLE
            val field: String = when (findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId) {
                R.id.radioIsbn -> "isbn"
                R.id.radioTitle -> "title"
                R.id.radioAuthor -> "author"
                else -> throw RuntimeException()
            }
            searchAsync.execute(field, findViewById<TextView>(R.id.editTextSearch).text.toString())
        }
    }

    private fun applyData(rawString: String) {
        val jsonData = JSONObject(rawString).getJSONArray("result")
        val dataset = arrayListOf<BookData>()
        for (i in 0 until jsonData.length()) {
            val data = jsonData.getJSONObject(i)
            dataset.add(BookData(data))
        }
        val rv = findViewById<RecyclerView>(R.id.recyclerView) as RecyclerView
        val adapter = CasarealRecycleViewAdapter(dataset)
        val llm = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.layoutManager = llm
        rv.adapter = adapter
        findViewById<ProgressBar>(R.id.progressBar3).visibility = View.INVISIBLE
    }

    private fun finishWithError() {
        AlertDialog.Builder(this).apply {
            setTitle("通信エラー")
            setMessage("""
                            通信エラーが発生しました。
                            通信状況を確認してください。
                        """.trimIndent())
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                finish()
            })
            show()
        }
    }
}

class SearchAsync : AsyncTask<String, Void, String>() {
    private var callbacktask: CallBackTask? = null

    override fun doInBackground(vararg params: String): String? {
        try {
            val url = URL(BASE_ADDR + "/api/books/search/${params[0]}/${params[1]}")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.instanceFollowRedirects = false
            con.doInput = true
            con.doOutput= false
            con.connect()

            val input = con.inputStream
            return readInputStream(input)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        callbacktask!!.callBack(result)
    }
    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }
    companion object{
        private const val TAG = "SearchAsync"
    }
    open class CallBackTask {
        open fun callBack(result: String?) {}
    }
}

class BookData(jsonData: JSONObject) {
    var title: String = "タイトル"
    var author: String = "著者"
    var status: Int = STATUS_UNKNOWN
    var isEnabled: Boolean = false

    init {
        title = jsonData.getString("title")
        author = jsonData.getString("author")
        status = jsonData.getString("status").toInt()
        isEnabled = status == STATUS_AVAILABLE || status == STATUS_ONLOAN
    }
    companion object {
        const val STATUS_UNKNOWN = 0
        const val STATUS_AVAILABLE = 1
        const val STATUS_ONLOAN = 2
        const val STATUS_NONE = 3
    }
}
