@file:JvmName("ReferenceValidator")

package com.jakewharton.sdksearch.reference.validator

import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import java.util.concurrent.TimeUnit.MINUTES
import java.util.regex.Pattern

private val PACKAGE = Pattern.compile("^([a-z0-9]+.)+")

fun main(vararg args: String) = runBlocking {
  val client = OkHttpClient()
  val service = DacComponent.builder()
      .baseUrl(BaseUrl(PRODUCTION_DAC))
      .client(client)
      .build()
      .documentationService()

  val fqcns = ITEM_LIST_URL_PATHS.values
      .map { service.list(it) }
      .flatMap { it.await() }
      .filter { it.type == "class" }
      .map { it.label }
      .sorted()

  val reference = AndroidReference(PRODUCTION_GIT_WEB)

  var pad = 0
  fun logStatus(message: String) {
    print("\r$message".padEnd(pad))
    pad = message.length + 1 // Account for \r
  }
  fun logPrint(message: String) {
    println("\r$message".padEnd(pad))
    pad = 0
  }

  var index = 0
  while (index < fqcns.size) {
    val percentage = (index.toFloat() / fqcns.size * 1000).toInt() / 10.0
    val checking = "Checking ${index + 1} of ${fqcns.size} ($percentage%)…"
    logStatus(checking)

    val fqcn = fqcns[index]
    val matcher = PACKAGE.matcher(fqcn)
    if (!matcher.lookingAt()) {
      throw IllegalArgumentException("FQCN '$fqcn' doesn't appear to be valid.")
    }
    val packageName = fqcn.substring(0, matcher.end() - 1)
    val className = fqcn.substring(matcher.end())

    val url = reference.sourceUrl(packageName, className)
    if (url != null) {
      val request = Builder().head().url(url).build()
      client.newCall(request).execute().use {
        val code = it.code()
        if (code == 429) {
          logStatus("$checking Rate limited! Cooling off…")
          delay(2, MINUTES)
          index -= 1 // Try this index again.
        } else if (!it.isSuccessful) {
          logPrint("$code $fqcn $url")
        }
      }
    }
    index++
  }

  logPrint("Checked $index references!")

  // Shut down OkHttpClient resources so that the JVM can exit cleanly.
  client.dispatcher().executorService().shutdown()
  client.connectionPool().evictAll();
}
