package com.tanaka.hondana.hondana

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