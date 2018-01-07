package com.chrome.platform.events

/** https://developer.chrome.com/extensions/events#type-Event */
@JsName("Event")
open external class Event2<out T1, out T2> : Event1<T1> {
  /** https://developer.chrome.com/extensions/events#method-Event-addListener */
  fun addListener(callback: (T1, T2) -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
  fun removeListener(callback: (T1, T2) -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
  fun hasListener(callback: (T1, T2) -> Unit)
}
