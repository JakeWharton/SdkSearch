package com.jakewharton.sdksearch

import com.chrome.platform.Chrome.omnibox
import com.chrome.platform.Chrome.storage
import com.chrome.platform.Chrome.tabs
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import kotlinx.coroutines.experimental.await
import kotlinx.coroutines.experimental.launch
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.js.json

fun main(vararg args: String) {
  omnibox.setDefaultSuggestion(
      DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

  omnibox.onInputChanged.addListener { text, _ ->
    println("OMNIBOX: \"$text\"")

    if (text.isEmpty()) {
      return@addListener
    }

    // TODO all the things!
  }

  omnibox.onInputEntered.addListener { text, _ ->
    val url = if (text.startsWith("http://") || text.startsWith("https://")) {
      text
    } else {
      "${PRODUCTION_DAC}index.html?q=$text"
    }

    tabs.update(UpdateProperties(url = url))
  }

  launch {
    val items = ITEM_LIST_URL_PATHS.values
        .map { PRODUCTION_DAC + it }
        .map { list(it) }
        .flatMap { it.await() }
        .filter { it.type == "class" }
        .sortedBy { it.label }

    storage.local.set(json("items" to items)) {
      println("Stored ${items.size} items")
    }

    println(items)
  }
}

private fun list(url: String): Promise<List<Item>> = window
    .fetch(url)
    .then {
      if (it.status != 200.toShort()) {
        throw RuntimeException("HTTP ${it.status} ${it.statusText}")
      } else {
        it.text()
      }
    }.then {
      // Data starts with a "val SOMETHING =" prefix which we skip.
      val startIndex = it.indexOf('=') + 1
      // Data ends with a ";<newline>" suffix which we skip.
      val endIndex = it.lastIndexOf(';')

      val json = it.substring(startIndex, endIndex)

      JSON.unquoted.parse(Item.serializer().list, json)
    }

@Serializable
data class Item(
  val id: Int,
  val label: String,
  val link: String,
  val type: String,
  @Serializable(with = BooleanStringSerializer::class) val deprecated: Boolean
)

@Serializer(forClass = Boolean::class)
object BooleanStringSerializer : KSerializer<Boolean> {
  override fun save(output: KOutput, obj: Boolean) = output.writeStringValue(obj.toString())
  // TODO https://youtrack.jetbrains.com/issue/KT-16348
  override fun load(input: KInput) = input.readStringValue().toLowerCase() == "true"
}
