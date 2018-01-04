package com.jakewharton.sdksearch.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.StyleSpan

inline fun buildSpannedString(builderAction: SpannableStringBuilder.() -> Unit): SpannedString {
  val builder = SpannableStringBuilder()
  builder.builderAction()
  return SpannedString(builder)
}

inline fun SpannableStringBuilder.inSpan(
  span: Any,
  builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder {
  val start = length
  builderAction()
  setSpan(span, start, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
  return this
}

inline fun SpannableStringBuilder.bold(builderAction: SpannableStringBuilder.() -> Unit) =
    inSpan(StyleSpan(Typeface.BOLD), builderAction)
