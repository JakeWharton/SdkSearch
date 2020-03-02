package com.jakewharton.sdksearch.proxy

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import com.squareup.moshi.JsonReader
import kotlin.time.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

private val dac = "https://developer.android.com"
private val urls = listOf(
    "$dac/reference/kotlin/classes?partial=1",
    "$dac/reference/kotlin/androidx/classes?partial=1",
    "$dac/reference/kotlin/androidx/ui/classes?partial=1",
    "$dac/reference/androidx/test/classes?partial=1",
    "$dac/reference/androidx/constraintlayout/classes?partial=1",
    "$dac/reference/com/google/android/material/classes?partial=1",
    "$dac/reference/com/google/android/play/core/classes?partial=1",
    "$dac/reference/com/android/billingclient/classes?partial=1",
    "$dac/reference/com/android/installreferrer/classes?partial=1"
)

suspend fun listDocumentedTypes(): List<DocumentedType> {
  return coroutineScope {
    urls.map { url -> async { listTypes(url) } }
        .awaitAll()
        .flatten()
  }
}

private val logger = LoggerFactory.getLogger("com.jakewharton.sdksearch.proxy")
private val client = OkHttpClient.Builder()
    .callTimeout(30.seconds)
    .addInterceptor(HttpLoggingInterceptor(object : Logger {
        override fun log(message: String) = logger.info(message)
      })
      .apply { level = BASIC }
    )
    .build()

private suspend fun listTypes(url: String): List<DocumentedType> {
  val request = Request.Builder().url(url).build()
  val call = client.newCall(request)

  val html = call.awaitBody().use {
    // The response is a JSON array of mixed types. The HTML we want is the string in index 2.
    val jsonReader = JsonReader.of(it.source())
    jsonReader.beginArray()
    jsonReader.skipValue()
    jsonReader.skipValue()
    jsonReader.nextString()
  }

  val document = Jsoup.parse(html)
  val elements = document.select("table tr")

  val documentedTypes = elements.map { element ->
    val linkElement = element.selectFirst("a")

    val deprecated = element.hasAttr("data-version-deprecated")
    val link = linkElement.attr("href")
        .removePrefix(dac) // Compose links include the base URL.
    val className = linkElement.text()

    // Parse the package name from the URL paths.
    val packageName = link
        .removePrefix("/reference/kotlin/")
        .removePrefix("/reference/")
        .removeSuffix(".html")
        .removeSuffix("/$className")
        .replace('/', '.')

    DocumentedType(packageName, className, deprecated, "$dac$link")
  }

  return documentedTypes
}
