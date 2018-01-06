@file:JsQualifier("chrome")

package com.jakewharton.sdksearch.chrome

/** https://developer.chrome.com/extensions/events */
external object events {
  sealed class Event {
    /** https://developer.chrome.com/extensions/events#method-Event-hasListeners */
    fun hasListeners(): Boolean

    /** https://developer.chrome.com/extensions/events#type-Event */
    @JsName("Event")
    open class Event0 : Event {
      /** https://developer.chrome.com/extensions/events#method-Event-addListener */
      fun addListener(callback: () -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
      fun removeListener(callback: () -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
      fun hasListener(callback: () -> Unit)
    }

    /** https://developer.chrome.com/extensions/events#type-Event */
    @JsName("Event")
    open class Event1<out T> : Event0 {
      /** https://developer.chrome.com/extensions/events#method-Event-addListener */
      fun addListener(callback: (T) -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
      fun removeListener(callback: (T) -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
      fun hasListener(callback: (T) -> Unit)
    }

    /** https://developer.chrome.com/extensions/events#type-Event */
    @JsName("Event")
    class Event2<out T1, out T2> : Event1<T1> {
      /** https://developer.chrome.com/extensions/events#method-Event-addListener */
      fun addListener(callback: (T1, T2) -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
      fun removeListener(callback: (T1, T2) -> Unit)

      /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
      fun hasListener(callback: (T1, T2) -> Unit)
    }
  }
}
