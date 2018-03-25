package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.STORAGE_KEY_DAC_URL
import com.jakewharton.sdksearch.STORAGE_KEY_GIT_WEB_URL
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.json

fun main(vararg args: String) {
  Chrome.storage.sync.get(json(
      STORAGE_KEY_GIT_WEB_URL to PRODUCTION_GIT_WEB,
      STORAGE_KEY_DAC_URL to PRODUCTION_DAC
  )) {
    val gitWebUrl = it[STORAGE_KEY_GIT_WEB_URL] as String
    val dacUrl = it[STORAGE_KEY_DAC_URL] as String

    val references = AndroidReference(gitWebUrl, dacUrl)

    val sourceUrl = references.sourceUrl(window.location.href) ?: return@get

    val br = document.createElement("br")

    val link = document.createElement("a")
    link.setAttribute("href", sourceUrl)
    link.textContent = "View source"

    val div = document.createElement("div")
    div.appendChild(br)
    div.appendChild(link)

    document.querySelector("#api-info-block")?.appendChild(div)
  }
}
