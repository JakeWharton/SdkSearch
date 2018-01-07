@file:JsQualifier("chrome.events")

package com.chrome.platform.events

/** https://developer.chrome.com/extensions/events#type-Event */
sealed external class Event {
  /** https://developer.chrome.com/extensions/events#method-Event-hasListeners */
  fun hasListeners(): Boolean
}

