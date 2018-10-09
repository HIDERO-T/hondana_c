package com.tanaka.hondana.hondana

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    /*
    private val RC_HANDLE_CAMERA_PERM = 2
    private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>? = null
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val barcode: String = intent.getStringExtra("barcode")
        applyData(barcode)

        findViewById<Button>(R.id.buttonBorrow).setOnClickListener{
            Toast.makeText(this, "借りました！", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.buttonReturn).setOnClickListener{
            Toast.makeText(this, "返しました！", Toast.LENGTH_SHORT).show()
            finish()
        }


    }



    private fun applyData(isbn: String){
        val bookstock = BookStock(isbn)
        val bookinfo = BookInfo(isbn)
        findViewById<TextView>(R.id.textIsbn).text = isbn
        findViewById<TextView>(R.id.textTitle).text = bookinfo.title!!
        findViewById<TextView>(R.id.textAuthor).text = bookinfo.author!!
        findViewById<TextView>(R.id.numberAll).text = bookstock.numberAll!!.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberOnloan).text = bookstock.numberOnloan!!.toString() + getString(R.string.book_unit)
        findViewById<TextView>(R.id.numberAvailable).text = bookstock.numberAvailable!!.toString() + getString(R.string.book_unit)
        findViewById<Button>(R.id.buttonBorrow).isEnabled = bookstock.canBorrow
        findViewById<Button>(R.id.buttonReturn).isEnabled = bookstock.canReturn!!
    }



}
