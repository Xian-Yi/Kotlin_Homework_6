package com.example.lab15

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLiteOpenHelper 子類別，用於管理資料庫的建立與升級
class MyDBHelper(
    context: Context,
    name: String = DB_NAME, // 預設資料庫名稱
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = VERSION // 預設資料庫版本
) : SQLiteOpenHelper(context, name, factory, version) {

    // 靜態常數儲存資料庫名稱與版本
    companion object {
        private const val DB_NAME = "myDatabase" // 資料庫名稱
        private const val VERSION = 1 // 資料庫版本
    }

    // 當資料庫首次建立時呼叫，用於建立初始的資料表
    override fun onCreate(db: SQLiteDatabase) {
        // 建立名為 myTable 的資料表，包含 book（文字主鍵）與 price（整數且不可為 NULL）
        val createTableSQL = """
            CREATE TABLE myTable(
                book TEXT PRIMARY KEY, 
                price INTEGER NOT NULL
            )
        """
        db.execSQL(createTableSQL)
    }

    // 當資料庫版本升級時呼叫，用於刪除舊資料表並建立新資料表
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 刪除舊資料表
        db.execSQL("DROP TABLE IF EXISTS myTable")
        // 重新建立資料表
        onCreate(db)
    }
}
