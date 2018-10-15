package com.tanaka.hondana.hondana

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.*
import java.util.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import android.os.AsyncTask.execute
import android.provider.Settings.Global.getString
import android.util.Log
import com.tanaka.hondana.hondana.GetBookStockAsync.CallBackTask
import java.lang.Thread.sleep
import org.json.JSONObject
import java.nio.file.Files.size


const val BASE_ADDR: String = "http://125.12.14.155:3000"

//TODO: Nullableでない設計に変更スべき。
class Book(data: JSONObject){
    var id: Int = 0
    var holder: String = ""
    var registerer: String = ""
    var duedate: Date = Date()
    init {
        id = data.getString("id").toInt()
        holder = data.getString("holder")
        registerer = data.getString("registerer")
        //duedate = SimpleDateFormat.parse(data.getString("duedate"))
    }
    companion object {
        const val HOLDBYME = 3
        const val ONLOAN = 2
        const val AVAILABLE = 1
    }
}

//TODO: userAccountを渡すのはややダサい
class BookStock(rawString: String, userAccount: String){
    var books: MutableList<Book> = mutableListOf()
    var numberAll: Int? = null
    var numberOnloan: Int? = null
    var numberAvailable: Int? = null
    var canBorrow: Boolean = false
    var canReturn: Boolean = false
    var toBeBorrowed: Int? = 0
    var toBeReturned: Int? = 0
    var isbn: String = ""
    var title: String = ""
    var author: String = ""

    init{
        val jsonData = JSONObject(rawString).getJSONArray("books")
        for (i in 0 until jsonData.length()) {
            val data = jsonData.getJSONObject(i)
            books.add(Book(data))
        }
        numberAll = books.size
        numberOnloan = books.count { it.holder != OFFICE }
        numberAvailable = books.count { it.holder == OFFICE }
        canReturn = books.count { it.holder == userAccount } > 0
        canBorrow = books.count { it.holder == OFFICE } >0
        toBeReturned = books.firstOrNull { it.holder == userAccount }?.id
        toBeBorrowed = books.firstOrNull { it.holder == OFFICE }?.id
        val jsonDataInfo = JSONObject(rawString).getJSONArray("infos").getJSONObject(0)
        isbn = jsonDataInfo.getString("isbn")
        title = jsonDataInfo.getString("title")
        author = jsonDataInfo.getString("author")
    }
    companion object{
        private const val OFFICE = "office@r-learning.co.jp"
    }
}

class GetBookStockAsync : AsyncTask<String, Void, String>() {
    private var callbacktask: CallBackTask? = null

    override fun doInBackground(vararg params: String): String? {
        var con: HttpURLConnection? = null
        var url: URL? = null

        try {
            url = URL(BASE_ADDR + "/api/books/isbn/${params[0]}")
            con = url!!.openConnection() as HttpURLConnection
            con!!.setRequestMethod("GET")
            con!!.setInstanceFollowRedirects(false)
            con!!.setDoInput(true)
            con!!.setDoOutput(false)
            con.connect()

            val `in` = con.getInputStream()
            val readSt = readInputStream(`in`)
            return readSt
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        callbacktask!!.callBack(result)
    }
    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }
    companion object{
        private const val TAG = "GetBookStockAsync"
    }
    open class CallBackTask {
        open fun callBack(result: String) {}
    }
}
class GetCoverImageAsync : AsyncTask<String, Void, Bitmap>() {
    private var callbacktask: CallBackTask? = null

    override fun doInBackground(vararg params: String): Bitmap? {
        var con: HttpURLConnection? = null
        var url: URL? = null

        try {
            url = URL(BASE_ADDR + "/api/images/${params[0]}")
            con = url!!.openConnection() as HttpURLConnection
            con!!.setRequestMethod("GET")
            con!!.setInstanceFollowRedirects(false)
            con!!.setDoInput(true)
            con!!.setDoOutput(false)
            con.connect()

            val `in` = con.getInputStream()
            Log.d(TAG, "GET OK.")
            val bitmap = BitmapFactory.decodeStream(`in`)
            return bitmap
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: Bitmap) {
        super.onPostExecute(result)
        callbacktask!!.callBack(result)
    }
    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }
    companion object{
        private const val TAG = "GetCoverImageAsync"
    }
    open class CallBackTask {
        open fun callBack(result: Bitmap) {}
    }
}

class BookBorrowReturnAsync : AsyncTask<String, Void, String>() {
    private var callbacktask: CallBackTask? = null

    override fun doInBackground(vararg params: String): String? {
        var con: HttpURLConnection? = null
        var url: URL? = null
        val id = params[1].toInt()
        val param = if (params[0] == BORROW) {
            "id=$id&user=${params[2]}"
        }else if (params[0] == RETURN){
            "id=$id"
        }else{
            throw RuntimeException()
        }

        try {
            url = URL(BASE_ADDR + "/api/books/${params[0]}")
            con = url!!.openConnection() as HttpURLConnection
            con!!.setRequestMethod("POST")
            con!!.setInstanceFollowRedirects(false)
            con!!.setDoInput(true)
            con!!.setDoOutput(true)
            con.connect()
            Log.d(TAG,"Connection OK")

            val outputStream = con.getOutputStream()
            val ps = PrintStream(con.getOutputStream())
            ps.print(param)
            ps.close()
            outputStream.close()
            Log.d(TAG, "Output OK")

            Log.d(TAG, "Status: ${con.getResponseCode()}")
            val `in` = con.getInputStream()
            Log.d(TAG, "GET OK.")
            val readSt = readInputStream(`in`)
            return readSt
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        callbacktask!!.callBack(result)
    }
    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }
    companion object{
        private const val TAG = "BookBorrowReturnAsync"
        private const val BORROW = "borrow"
        private const val RETURN = "return"
    }
    open class CallBackTask {
        open fun callBack(result: String) {}
    }
}

@Throws(IOException::class, UnsupportedEncodingException::class)
fun readInputStream(`in`: InputStream): String {
    val sb = StringBuffer()
    var st: String? = ""

    val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
    st = br.readLine()
    while (st != null) {
        sb.append(st)
        st = br.readLine()
    }
    try {
        `in`.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return sb.toString()
}
