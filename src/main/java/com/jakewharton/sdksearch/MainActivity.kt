package com.jakewharton.sdksearch

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import java.io.IOException

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val moshi = Moshi.Builder().build()
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://developer.android.com/reference/lists.js")
        .build()
    val call = client.newCall(request)
    call.enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (!response.isSuccessful) {
          runOnUiThread { throw RuntimeException("HTTP ${response.code()}") }
          return
        }
        val source = response.body()!!.source()
        val start = source.indexOf(ByteString.encodeUtf8("["))
        source.skip(start)

        val type = Types.newParameterizedType(List::class.java, Item::class.java)
        val adapter = moshi.adapter<List<Item>>(type)
        val reader = JsonReader.of(source)
        reader.isLenient = true

        val items = adapter.fromJson(reader)!!

        items.forEach {
          Log.d("Item", it.toString())
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        runOnUiThread { throw RuntimeException(e) }
      }
    })
  }
}
