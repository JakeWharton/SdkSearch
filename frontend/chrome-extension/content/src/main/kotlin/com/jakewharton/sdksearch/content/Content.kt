package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.sourceUrl
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import timber.log.ConsoleTree
import timber.log.Timber
import timber.log.debug
import kotlin.browser.document
import kotlin.browser.window

fun main() {
  Timber.plant(ConsoleTree())

  val sourceUrl = AndroidReference.sourceUrl(window.location.href)
  if (sourceUrl == null) {
    Timber.debug { "No source URL for ${window.location.href}" }
    return
  }

  val observer = MutationObserver { _, self ->
    val targetElement = document.querySelector("#api-info-block .api-level")
    if (targetElement != null) {
      self.disconnect() // Only append link once.

      val br = document.createElement("br")
      targetElement.appendChild(br)

      val link = document.createElement("a")
      link.setAttribute("href", sourceUrl)
      link.textContent = "view source"

      val manifest = Chrome.runtime.getManifest()
      if ("Debug" in manifest["name"].unsafeCast<String>()) {
        link.textContent += " (debug)"
      }

      targetElement.appendChild(link)
    } else {
      Timber.debug { "Could not find on-page element to add 'view source' link" }
    }
  }
  observer.observe(document, MutationObserverInit(childList = true, subtree = true))
}
