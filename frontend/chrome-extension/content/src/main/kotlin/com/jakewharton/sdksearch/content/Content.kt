package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.sourceUrl
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Element
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import timber.log.ConsoleTree
import timber.log.Timber
import timber.log.debug
import kotlin.browser.document
import kotlin.browser.window
import kotlin.coroutines.resume

suspend fun main() {
  Timber.plant(ConsoleTree())

  val sourceUrl = AndroidReference.sourceUrl(window.location.href)
  if (sourceUrl == null) {
    Timber.debug { "No source URL for ${window.location.href}" }
    return
  }

  val targetElement = document.awaitElement("#api-info-block .api-level")

  val br = document.createElement("br")
  targetElement.appendChild(br)

  val link = document.createElement("a")
  link.setAttribute("href", sourceUrl)
  link.textContent = "view source"

  if ("Debug" in Chrome.runtime.manifest["name"].unsafeCast<String>()) {
    link.textContent += " (debug)"
  }

  targetElement.appendChild(link)
}

private suspend fun <T> T.awaitElement(selectors: String): Element where T : Node, T : ParentNode {
  return suspendCancellableCoroutine { continuation ->
    val observer = MutationObserver { _, self ->
      querySelector(selectors)?.let {
        self.disconnect()
        continuation.resume(it)
      }
    }
    continuation.invokeOnCancellation {
      observer.disconnect()
    }
    observer.observe(this, MutationObserverInit(childList = true, subtree = true))
  }
}
