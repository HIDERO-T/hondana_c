package com.tanaka.hondana.hondana

import android.Manifest
import android.accounts.*
import android.accounts.AccountManager.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.tanaka.hondana.hondana.barcodereader.BarcodeCaptureActivity
import com.tanaka.hondana.hondana.barcodereader.BarcodeGraphic
import com.tanaka.hondana.hondana.barcodereader.BarcodeGraphicTracker
import com.tanaka.hondana.hondana.barcodereader.BarcodeTrackerFactory
import com.tanaka.hondana.hondana.barcodereader.ui.camera.CameraSource
import com.tanaka.hondana.hondana.barcodereader.ui.camera.CameraSourcePreview
import com.tanaka.hondana.hondana.barcodereader.ui.camera.GraphicOverlay
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import kotlinx.android.synthetic.main.content_main2.*
import java.io.IOException
import java.util.regex.Pattern

class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BarcodeGraphicTracker.BarcodeUpdateListener, BlankFragment.OnFragmentInteractionListener  {

    var userAccount: String? = null

    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>? = null

    // helper objects for detecting taps and pinches.
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
//        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //separator

        nav_view.setNavigationItemSelectedListener(this)
        mPreview = findViewById<View>(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById<View>(R.id.graphicOverlay) as GraphicOverlay<BarcodeGraphic>

        // read parameters from the intent used to launch the activity.
        val autoFocus = intent.getBooleanExtra(BarcodeCaptureActivity.AutoFocus, true)
        val useFlash = intent.getBooleanExtra(BarcodeCaptureActivity.UseFlash, false)

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        var lst: MutableList<String> = mutableListOf()
        var rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            Log.d(TAG, "Camera not granted")
            lst.add(Manifest.permission.CAMERA)
        }
        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
        if (rc != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Acc not granted")
            lst.add(Manifest.permission.GET_ACCOUNTS)
        }
        if (lst.size != 0) requestPermission(lst)

        gestureDetector = GestureDetector(this, CaptureGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        if(Build.VERSION.SDK_INT <= 22){
            userAccount = AccountManager.get(this).getAccountsByType("com.google")[0].name
        }else if(Build.VERSION.SDK_INT >= 23){
            val intent: Intent = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null,
                    null)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_main -> {
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_register -> {

            }
            R.id.nav_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
            R.id.nav_list -> {
                startActivity(Intent(this, ListActivity::class.java))
            }
            R.id.nav_config -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    //sep

    private fun requestPermission(lst: MutableList<String>){
        Log.w( Main2Activity.TAG, "Some permission is not granted. Requesting permission")

        val permissions = lst.toTypedArray()

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, Main2Activity.RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, permissions,
                    Main2Activity.RC_HANDLE_CAMERA_PERM)
        }

        findViewById<View>(R.id.topLayout).setOnClickListener(listener)
        Snackbar.make(mGraphicOverlay!!, "カメラのパーミッション！",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_CODE) {
            val received = data!!
            userAccount = received.extras.get(KEY_ACCOUNT_NAME).toString()
            Toast.makeText(this, "${userAccount}でログインしました。", Toast.LENGTH_LONG).show()
            findViewById<TextView>(R.id.textAccount).text = userAccount
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<ProgressBar>(R.id.progressBar2).visibility = View.INVISIBLE
        startCameraSource()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e)

        val c = gestureDetector!!.onTouchEvent(e)

        return b || c || super.onTouchEvent(e)
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(mGraphicOverlay, this)
        barcodeDetector.setProcessor(
                MultiProcessor.Builder(barcodeFactory).build())

        if (!barcodeDetector.isOperational) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w( Main2Activity.TAG, "Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(this, "ストレージ少ないよ！", Toast.LENGTH_LONG).show()
                Log.w( Main2Activity.TAG, "ストレージ少ないよ！")
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        var builder: CameraSource.Builder = CameraSource.Builder(applicationContext, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null)
        }

        mCameraSource = builder
                .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
                .build()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        if (mPreview != null) {
            mPreview!!.stop()
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mPreview != null) {
            mPreview!!.release()
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in [.requestPermissions].
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
    //TODO: グチャグチャ。リファクタ必要。

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_ACCOUNT){
            if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Account permission granted")
                return
            }
        } else if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BOTh permission granted - initialize the camera source")
                // we have permission, so create the camerasource
                val autoFocus = intent.getBooleanExtra(AutoFocus, false)
                val useFlash = intent.getBooleanExtra(UseFlash, false)
                createCameraSource(autoFocus, useFlash)
                return
            }
        } else {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multitracker sample")
                .setMessage("カメラのパーミッション！")
                .setPositiveButton("OK", listener)
                .show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private fun onTap(rawX: Float, rawY: Float): Boolean {
        // Find tap point in preview frame coordinates.
        val location = IntArray(2)
        mGraphicOverlay!!.getLocationOnScreen(location)
        val x = (rawX - location[0]) / mGraphicOverlay!!.widthScaleFactor
        val y = (rawY - location[1]) / mGraphicOverlay!!.heightScaleFactor

        // Find the barcode whose center is closest to the tapped point.
        var best: Barcode? = null
        var bestDistance = java.lang.Float.MAX_VALUE
        for (graphic in mGraphicOverlay!!.graphics) {
            val barcode = graphic.barcode
            if (barcode.boundingBox.contains(x.toInt(), y.toInt())) {
                // Exact hit, no need to keep looking.
                best = barcode
                break
            }
            val dx = x - barcode.boundingBox.centerX()
            val dy = y - barcode.boundingBox.centerY()
            val distance = dx * dx + dy * dy  // actually squared distance
            if (distance < bestDistance) {
                best = barcode
                bestDistance = distance
            }
        }

        if (best != null) {
            val data = Intent()
            data.putExtra(BarcodeObject, best)
            setResult(CommonStatusCodes.SUCCESS, data)
            finish()
            return true
        }
        return false
    }

    private inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return false
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         *
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mCameraSource!!.doZoom(detector.scaleFactor)
        }
    }

    override fun onBarcodeDetected(barcode: Barcode) {
        //do something with barcode data returned
        findViewById<ProgressBar>(R.id.progressBar2).visibility = View.VISIBLE

        if (!isIsbn(barcode.rawValue)) return

        //TODO: フラグメントどうするか検討。
        val intent: Intent = Intent(this, MainActivity::class.java)
        intent.putExtra("barcode", barcode.rawValue)
        intent.putExtra("userAccount", userAccount)
        startActivity(intent)
    }

    private fun isIsbn(barcode: String) = Pattern.compile("^97[89]").matcher(barcode).find()

    override fun onFragmentInteraction(uri: Uri) {
    }

    companion object {
        private val TAG = "MainActivity2"

        // intent request code to handle updating play services if needed.
        private val RC_HANDLE_GMS = 9001

        // permission request codes need to be < 256
        private val RC_HANDLE_CAMERA_PERM = 2

        private val RC_ACCOUNT = 15

        private val REQUEST_CODE = 4
        // constants used to pass extra data in the intent
        val AutoFocus = "AutoFocus"
        val UseFlash = "UseFlash"
        val BarcodeObject = "Barcode"
    }


}
