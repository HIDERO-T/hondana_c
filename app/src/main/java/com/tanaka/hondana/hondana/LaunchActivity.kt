package com.tanaka.hondana.hondana

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.tanaka.hondana.hondana.barcodereader.BarcodeCaptureActivity

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        findViewById<Button>(R.id.rental).setOnClickListener{
            val intent: Intent = Intent(this, BarcodeCaptureActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.settings).setOnClickListener{
            val intent: Intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


    }
}
