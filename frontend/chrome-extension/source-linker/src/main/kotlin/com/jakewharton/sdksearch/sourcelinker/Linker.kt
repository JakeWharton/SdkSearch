package com.jakewharton.sdksearch.sourcelinker

import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.sourceUrl
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import kotlin.coroutines.resume
import timber.log.Timber
import timber.log.debug

suspend fun Document.insertSourceLink(windowUrl: String, isDebug: Boolean = false): Boolean {
  val sourceUrl = AndroidReference.sourceUrl(windowUrl)
  if (sourceUrl == null) {
    Timber.debug { "No source URL for $windowUrl" }
    return false
  }

  val targetElement = awaitElement("#api-info-block .api-level")

  if (targetElement.hasChildNodes()) {
    val br = createElement("br")
    targetElement.appendChild(br)
  }

  val link = createElement("a")
  link.setAttribute("href", sourceUrl)
  link.textContent = "view source"

  if (isDebug) {
    link.textContent += " (debug)"
  }

  targetElement.appendChild(link)
  return true
}

private suspend fun <T> T.awaitElement(selectors: String): Element where T : Node, T : ParentNode {
  querySelector(selectors)?.let { return it }

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
