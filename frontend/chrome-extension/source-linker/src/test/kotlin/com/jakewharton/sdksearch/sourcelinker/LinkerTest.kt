package com.jakewharton.sdksearch.sourcelinker

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.get

class LinkerTest {
  private lateinit var document: Document
  private lateinit var body: Element

  @BeforeTest fun before() {
    val jsDom = JsDom("<html><body></body></html>")
    document = jsDom.window.document
    body = document.querySelector("body")!!

    global["MutationObserver"] = jsDom.window["MutationObserver"]
  }

  @Test fun noLinkForUnknownHost() = testBody {
    assertFalse(document.insertSourceLink("https://example.com"))
  }

  @Test fun noLinkForUnknownDacUrl() = testBody {
    assertFalse(document.insertSourceLink("https://developer.android.com/something/else"))
  }

  @Test fun noLinkForUnknownType() = testBody {
    assertFalse(document.insertSourceLink("https://developer.android.com/reference/com/example/Foo"))
  }

  @Test fun linkWaitsForDomElement() = testBody {
    // Start undispatched to ensure suspension occurs.
    val result = async(start = UNDISPATCHED) {
      document.insertSourceLink("https://developer.android.com/reference/android/widget/Toolbar")
    }
    assertFalse(result.isCompleted)

    val apiLevelDiv = addInfoDivToBody()

    assertTrue(result.await())

    val childNodes = apiLevelDiv.childNodes
    assertEquals(1, childNodes.length)
    val node = childNodes[0]!!
    assertEquals("A", node.nodeName)
    val anchor = node.unsafeCast<HTMLAnchorElement>()
    assertEquals("view source", anchor.text)
    assertEquals("https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", anchor.href)
  }

  @Test fun linkDisplaysDebug() = testBody {
    val apiLevelDiv = addInfoDivToBody()
    document.insertSourceLink("https://developer.android.com/reference/android/widget/Toolbar", isDebug = true)

    val childNodes = apiLevelDiv.childNodes
    assertEquals(1, childNodes.length)
    val anchor = childNodes[0].unsafeCast<HTMLAnchorElement>()
    assertEquals("view source (debug)", anchor.text)
  }

  @Test fun linkAddsBrWhenDivNotEmpty() = testBody {
    val apiLevelDiv = addInfoDivToBody()
    apiLevelDiv.innerHTML = """added in <a href="#">API level 21</a>"""

    document.insertSourceLink("https://developer.android.com/reference/android/widget/Toolbar")

    val childNodes = apiLevelDiv.childNodes
    assertEquals(4, childNodes.length)
    assertEquals("BR", childNodes[2]!!.nodeName)
    assertEquals("A", childNodes[3]!!.nodeName)
  }

  private fun addInfoDivToBody(): Element {
    val apiInfoDiv = document.createElement("div").apply {
      id = "api-info-block"
    }
    body.appendChild(apiInfoDiv)

    val apiLevelDiv = document.createElement("div").apply {
      classList.add("api-level")
    }
    apiInfoDiv.appendChild(apiLevelDiv)

    return apiLevelDiv
  }
}
