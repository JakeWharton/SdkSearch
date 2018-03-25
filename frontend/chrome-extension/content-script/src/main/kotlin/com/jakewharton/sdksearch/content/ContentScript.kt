package com.jakewharton.sdksearch.content

import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import kotlin.browser.document
import kotlin.browser.window

val references = AndroidReference(PRODUCTION_GIT_WEB, PRODUCTION_DAC)

fun main(vararg args: String) {
  val sourceUrl = references.sourceUrl(window.location.href) ?: return

  val br = document.createElement("br")

  val link = document.createElement("a")
  link.setAttribute("href", sourceUrl)
  link.textContent = "View source"

  val div = document.createElement("div")
  div.appendChild(br)
  div.appendChild(link)

  document.querySelector("#api-info-block")?.appendChild(div)
}
