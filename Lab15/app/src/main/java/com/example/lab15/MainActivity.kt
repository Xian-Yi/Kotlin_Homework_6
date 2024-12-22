package com.example.lab15

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupWindowInsets()

        // 初始化資料庫與清單視圖
        initDatabase()
        initListView()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close() // 關閉資料庫
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initDatabase() {
        dbrw = MyDBHelper(this).writableDatabase
    }

    private fun initListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter
    }

    private fun setListeners() {
        val edBook = findViewById<EditText>(R.id.edBook)
        val edPrice = findViewById<EditText>(R.id.edPrice)

        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            performDatabaseOperation(
                edBook, edPrice, "INSERT",
                operation = { book, price ->
                    dbrw.execSQL(
                        "INSERT INTO myTable(book, price) VALUES(?, ?)", arrayOf(book, price)
                    )
                },
                successMessage = { "新增: $it" }
            )
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            performDatabaseOperation(
                edBook, edPrice, "UPDATE",
                operation = { book, price ->
                    dbrw.execSQL(
                        "UPDATE myTable SET price = ? WHERE book LIKE ?", arrayOf(price, book)
                    )
                },
                successMessage = { "更新: $it" }
            )
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            performDatabaseOperation(
                edBook, null, "DELETE",
                operation = { book, _ ->
                    dbrw.execSQL("DELETE FROM myTable WHERE book LIKE ?", arrayOf(book))
                },
                successMessage = { "刪除: $it" }
            )
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val query = if (edBook.text.isEmpty()) "SELECT * FROM myTable"
            else "SELECT * FROM myTable WHERE book LIKE ?"

            val cursor = dbrw.rawQuery(query, if (edBook.text.isEmpty()) null else arrayOf(edBook.text.toString()))
            displayQueryResults(cursor)
        }
    }

    private fun performDatabaseOperation(
        edBook: EditText,
        edPrice: EditText?,
        operationType: String,
        operation: (String, String?) -> Unit,
        successMessage: (String) -> String
    ) {
        val book = edBook.text.toString()
        val price = edPrice?.text?.toString()

        if (book.isEmpty() || (edPrice != null && price.isNullOrEmpty())) {
            showToast("欄位請勿留空")
            return
        }

        try {
            operation(book, price)
            showToast(successMessage(book))
            clearInputFields()
        } catch (e: Exception) {
            showToast("$operationType 失敗: $e")
        }
    }

    private fun displayQueryResults(cursor: android.database.Cursor) {
        cursor.use {
            items.clear()
            showToast("共有 ${it.count} 筆資料")
            while (it.moveToNext()) {
                items.add("書名: ${it.getString(0)}\t\t價格: ${it.getInt(1)}")
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun clearInputFields() {
        findViewById<EditText>(R.id.edBook).text.clear()
        findViewById<EditText>(R.id.edPrice).text.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
