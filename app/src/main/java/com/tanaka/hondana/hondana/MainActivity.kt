package com.tanaka.hondana.hondana

import android.content.DialogInterface
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.*
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

    private var toBeBorrowed: Int? = null
    private var toBeReturned: Int? = null
    var userAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val barcode: String = intent.getStringExtra("barcode")
        userAccount = intent.getStringExtra("userAccount")

        val getBookStockAsync = GetBookStockAsync()
        getBookStockAsync.setOnCallBack(object : GetBookStockAsync.CallBackTask() {
            override fun callBack(result: String?) {
                super.callBack(result)
                if (result == null) {
                    finishWithError()
                }else{
                    val bookStock = BookStock(result, userAccount!!)
                    findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
                    applyData(bookStock)
                }
            }
        })
        val getCoverImageAsync = GetCoverImageAsync()
        getCoverImageAsync.setOnCallBack(object : GetCoverImageAsync.CallBackTask() {
            override fun callBack(result: Bitmap?) {
                super.callBack(result)
                if (result != null) {
                    findViewById<ProgressBar>(R.id.progressBarImage).visibility = View.INVISIBLE
                    findViewById<ImageView>(R.id.img).setImageBitmap(result)
                }
            }
        })

        val bookBorrowReturnAsync = BookBorrowReturnAsync()
        bookBorrowReturnAsync.setOnCallBack(object : BookBorrowReturnAsync.CallBackTask() {
            override fun callBack(result: String?) {
                super.callBack(result)
                if (result == null) {
                    finishWithError()
                }else {
                    //FIXME: 正常に動かない。
                    //Toast.makeText(this, "処理が正常終了しました。", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        })

        findViewById<Button>(R.id.buttonBorrow).setOnClickListener{
            if (toBeBorrowed == null) {
                Toast.makeText(this, "その本は借りられません。", Toast.LENGTH_SHORT).show()
            }else {
                bookBorrowReturnAsync.execute("borrow", toBeBorrowed.toString(), userAccount)
            }
        }
        findViewById<Button>(R.id.buttonReturn).setOnClickListener{
            if (toBeReturned == null) {
                Toast.makeText(this, "その本は返せません。", Toast.LENGTH_SHORT).show()
            }else {
                bookBorrowReturnAsync.execute("return", toBeReturned.toString())
            }
        }

        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        getBookStockAsync.execute(barcode)
        getCoverImageAsync.execute(barcode)
    }

    private fun applyData(bookStock: BookStock) {
        findViewById<TextView>(R.id.textIsbn).text = bookStock.isbn
        findViewById<TextView>(R.id.textTitle).text = bookStock.title
        findViewById<TextView>(R.id.textAuthor).text = bookStock.author
        findViewById<TextView>(R.id.numberAll).text = bookStock.numberAll.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberOnloan).text = bookStock.numberOnloan.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberAvailable).text = bookStock.numberAvailable.toString() + getString(R.string.book_unit)
        toBeBorrowed = bookStock.toBeBorrowed
        toBeReturned = bookStock.toBeReturned
        findViewById<Button>(R.id.buttonBorrow).isEnabled = bookStock.canBorrow
        findViewById<Button>(R.id.buttonReturn).isEnabled = bookStock.canReturn
    }

    private fun finishWithError(){
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
