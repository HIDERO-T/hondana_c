package com.tanaka.hondana.hondana

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor



class MainActivity : AppCompatActivity() {
    /*
    private val RC_HANDLE_CAMERA_PERM = 2
    private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>? = null
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FIXME: なぜか動かない。
        //findViewById<ConstraintLayout>(R.id.operationBox).visibility = View.GONE
        setContentView(R.layout.activity_main)
        /*
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false)
        } else {
            requestCameraPermission()
        }
        */



        findViewById<View>(R.id.scanner).setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View){
                //TODO: スキャナ完成したらイベントを変更。
                val operationBox: ConstraintLayout = findViewById(R.id.operationBox)
                if (operationBox.visibility == View.GONE)
                    operationBox.visibility = View.VISIBLE
                else
                    operationBox.visibility = View.GONE

            }
        })

        findViewById<FloatingActionButton>(R.id.toSettings).setOnClickListener{
                val img: ImageView = findViewById<ImageView>(R.id.bimage)
                val bmp: Bitmap = BitmapFactory.decodeResource(
                                                applicationContext.resources, R.drawable.puppy)
                img.setImageBitmap(bmp)

                val detector = BarcodeDetector.Builder(applicationContext)
                        .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
                        .build()
                if (!detector.isOperational) {
                    Log.d("BARCODE", "Could not set up the detector!")
                    //return
                }
                val frame = Frame.Builder().setBitmap(bmp).build()
                val barcodes = detector.detect(frame)
                val thisCode = barcodes.valueAt(0)
                val txtView = findViewById(R.id.isbn) as TextView
                txtView.text = thisCode.rawValue

                //FIXME: 動かない
                //val intent: Intent = Intent(this, barcodereader.BarcodeCaptureActivity::class.java)
                //startActivity(intent)

                //TODO: 上は本当はBarcodeDetected。下記に差し替える。
                //val intent: Intent = Intent(this, SettingsActivity::class.java)
                //startActivity(intent)
        }
    }

    fun applyData(isbn: Int){
        //TODO: API接続処理

    }



}
