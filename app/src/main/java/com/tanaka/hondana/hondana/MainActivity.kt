package com.tanaka.hondana.hondana

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.gms.tasks.Tasks.await
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.*


class MainActivity : AppCompatActivity() {
    /*
    private val RC_HANDLE_CAMERA_PERM = 2
    private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>? = null
    */

    var toBeBorrowed: Int? = null
    var toBeReturned: Int? = null
    var userAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val barcode: String = intent.getStringExtra("barcode")
        userAccount = intent.getStringExtra("userAccount")

        applyData(barcode)

        findViewById<Button>(R.id.buttonBorrow).setOnClickListener{
            if (toBeBorrowed == null) {
                Toast.makeText(this, "その本は借りられません。", Toast.LENGTH_SHORT).show()
            }else {
                val url = "$BASE_ADDR/api/books/borrow"
                Fuel.post(url, listOf("id" to toBeBorrowed, "user" to userAccount)).response { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            Toast.makeText(this, "借りました！", Toast.LENGTH_SHORT).show()
                        }
                        is Result.Failure -> {
                            println("通信に失敗しました。")
                        }
                    }
                }
                finish()
            }
        }

        findViewById<Button>(R.id.buttonReturn).setOnClickListener{
            if (toBeReturned == null) {
                Toast.makeText(this, "その本は返せません。", Toast.LENGTH_SHORT).show()
            }else {
                val url = "$BASE_ADDR/api/books/return"
                Fuel.post(url, listOf("id" to toBeReturned)).response { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            Toast.makeText(this, "返しました！", Toast.LENGTH_SHORT).show()
                        }
                        is Result.Failure -> {
                            println("通信に失敗しました。")
                        }
                    }
                }
                finish()
            }
        }


        //FIXME: 非同期プロセス終了まで待つために5秒のsleep
        //FIXME: ひどすぎる。。。
        sleep(3000)
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
    }

    private fun applyData(isbn: String) {

        val url = "$BASE_ADDR/api/books/isbn/$isbn"

            url.httpGet().response { request, response, result ->
                when (result) {
                    is Result.Success -> {
                        // TODO: 処理が煩雑、どこかに移したい。
                        val json = String(response.data)
                        val mapper = jacksonObjectMapper()
                        val books = mapper.readValue<Books>(json)
                        println(books)
                        applyView(books)
                    }
                    is Result.Failure -> {
                        println("通信に失敗しました。")
                    }
                }

            }
    }

    private fun applyView(books: Books){
        findViewById<TextView>(R.id.textIsbn).text = books.books[0].isbn
        //findViewById<TextView>(R.id.textTitle).text = books.books[0].title
        //findViewById<TextView>(R.id.textAuthor).text = author
        findViewById<TextView>(R.id.numberAll).text = books.books.size.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberOnloan).text = books.books.count{it.holder != "office@r-learning.co.jp"}.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberAvailable).text = books.books.count{it.holder == "office@r-learning.co.jp"}.toString() + getString(R.string.book_unit)
        println(books.books.firstOrNull{it.holder == "office@r-learning.co.jp"}?.id.toString())
        toBeBorrowed = books.books.firstOrNull{it.holder == "office@r-learning.co.jp"}?.id
        toBeReturned = books.books.firstOrNull{it.holder == userAccount}?.id
        findViewById<Button>(R.id.buttonBorrow).isEnabled = ( books.books.count{it.holder == "office@r-learning.co.jp"} > 0)
        findViewById<Button>(R.id.buttonReturn).isEnabled = (books.books.count{it.holder == userAccount} > 0)

    }

    companion object{
        private const val BASE_ADDR = "http://125.12.14.155:3000"
        private const val TEMP_USER_NAME = "android.studio@android.com"
    }

}
data class Books(val books: Array<Book>)
data class Book(val id: Int, val isbn: String, val holder: String, val registerer: String,
                val duedate: Date, val created_at: Date, val updated_at: Date)
