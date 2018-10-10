package com.tanaka.hondana.hondana

import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import android.os.AsyncTask
import java.io.*


//TODO: Nullableでない設計に変更スべき。

class Book(id: Int){
    var status: Int? = null
    var registerer: String? = null
    var holder: String? = null
    init{
        //TODO:API接続
        registerer = "a-saito@r-learning.co.jp"
        holder = "h-tanaka@r-learning.co.jp"

        //TODO:ログインできてから。
        status = AVAILABLE
    }

    companion object {
        const val HOLDBYME = 3
        const val ONLOAN = 2
        const val AVAILABLE = 1
    }
}

class BookStock(isbn: String){
    var books: List<Book>? = null
    var numberAll: Int? = null
    var numberOnloan: Int? = null
    var numberAvailable: Int? = null
    var canBorrow: Boolean = false
    var canReturn: Boolean? = false

    init{
        //TODO:API接続
        books = listOf(Book(1), Book(2))
        numberAll = books?.size
        numberOnloan = books?.count { it.status == Book.ONLOAN }
        numberAvailable = books?.count { it.status == Book.AVAILABLE }
        canBorrow = (numberAvailable!! > 0)
        canReturn = books?.any{ it.status == Book.HOLDBYME }
    }
}

class BookInfo(isbn: String){
    var title: String? = null
    var author: String? = null
    init{
        //TODO: API接続
        title = "Ruby公式"
        author = "増井雄一郎"
    }
}

class HttpResponsAsync : AsyncTask<Void, Void, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        // doInBackground前処理
    }

    override fun doInBackground(vararg params: Void): String? {
        var con: HttpURLConnection? = null
        var url: URL? = null
        val urlSt = "http://125.12.14.155:3000/books/"

        try {
            // URLの作成
            url = URL(urlSt)
            // 接続用HttpURLConnectionオブジェクト作成
            con = url!!.openConnection() as HttpURLConnection
            // リクエストメソッドの設定
            con!!.setRequestMethod("GET")
            // リダイレクトを自動で許可しない設定
            con!!.setInstanceFollowRedirects(false)
            // URL接続からデータを読み取る場合はtrue
            con!!.setDoInput(true)
            // URL接続にデータを書き込む場合はtrue
            con!!.setDoOutput(true)

            // 接続
            con!!.connect() // ①

        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        return null
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        // doInBackground後処理
    }

}

@Throws(IOException::class, UnsupportedEncodingException::class)
fun readInputStream(`in`: InputStream): String {
    val sb = StringBuffer()
    var st = ""

    val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
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