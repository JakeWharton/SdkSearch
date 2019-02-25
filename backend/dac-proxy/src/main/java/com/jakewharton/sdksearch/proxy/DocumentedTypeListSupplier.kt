package com.jakewharton.sdksearch.proxy

import com.google.common.base.Supplier
import com.jakewharton.sdksearch.proxy.model.DocumentedType
import com.squareup.moshi.JsonReader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import org.jsoup.Jsoup

internal object DocumentedTypeListSupplier : Supplier<List<DocumentedType>> {
  private val dac = "https://developer.android.com"
  private val urls = listOf(
      "$dac/reference/classes?partial=1",
      "$dac/reference/androidx/classes?partial=1",
      "$dac/reference/androidx/test/classes?partial=1",
      "$dac/reference/com/google/android/material/classes?partial=1",
      "$dac/reference/com/google/android/play/core/classes?partial=1",
      "$dac/reference/com/android/billingclient/classes?partial=1",
      "$dac/reference/com/android/installreferrer/classes?partial=1"
  )

  private val client = OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor(::println).setLevel(BASIC))
      .build()

  override fun get(): List<DocumentedType> {
    return urls.map(::listTypes).flatten()
  }

  private fun listTypes(url: String): List<DocumentedType> {
    val request = Request.Builder().url(url).build()
    val call = client.newCall(request)

    val html = call.execute().use {
      // The response is a JSON array of mixed types. The HTML we want is the string in index 2.
      val jsonReader = JsonReader.of(it.body()!!.source())
      jsonReader.beginArray()
      jsonReader.skipValue()
      jsonReader.skipValue()
      jsonReader.nextString()
    }

    val document = Jsoup.parse(html)
    val elements = document.select("table tr")

    val documentedTypes = elements.map { element ->
      val linkElement = element.selectFirst("a")

      val api = element.attr("data-version-added").toIntOrNull() ?: 0
      val deprecated = element.hasAttr("data-version-deprecated")
      val link = linkElement.attr("href")
      val className = linkElement.text()

      // Parse the package name from the URL paths.
      val packageName = link.removePrefix("https://developer.android.com/reference/")
          .removeSuffix("/$className.html")
          .replace('/', '.')

      DocumentedType(packageName, className, api, deprecated, link)
    }

    return documentedTypes
  }
}
