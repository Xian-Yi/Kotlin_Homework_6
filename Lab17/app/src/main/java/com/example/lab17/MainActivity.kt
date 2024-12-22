package com.example.lab17

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btnQuery: Button
    private val client = OkHttpClient() // 單一 OkHttpClient 實例重用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupWindowInsets()
        initUI()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUI() {
        btnQuery = findViewById(R.id.btnQuery)
        btnQuery.setOnClickListener {
            btnQuery.isEnabled = false // 防止重複點擊
            fetchAirQualityData()
        }
    }

    private fun fetchAirQualityData() {
        val url = "https://api.italkutalk.com/api/air"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    try {
                        val myObject = Gson().fromJson(json, MyObject::class.java)
                        showResultDialog(myObject)
                    } catch (e: Exception) {
                        handleError("資料解析錯誤：$e")
                    }
                } else {
                    handleError("回傳資料為空")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                handleError("查詢失敗：$e")
            }
        })
    }

    private fun showResultDialog(myObject: MyObject) {
        val items = myObject.result.records.map { "地區：${it.SiteName}, 狀態：${it.Status}" }.toTypedArray()

        runOnUiThread {
            btnQuery.isEnabled = true
            AlertDialog.Builder(this)
                .setTitle("臺北市空氣品質")
                .setItems(items, null)
                .show()
        }
    }

    private fun handleError(message: String) {
        runOnUiThread {
            btnQuery.isEnabled = true
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
