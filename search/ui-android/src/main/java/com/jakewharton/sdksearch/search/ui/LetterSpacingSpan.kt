package com.jakewharton.sdksearch.search.ui

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

internal class LetterSpacingSpan(private val letterSpacing: Float = 0.0f) : MetricAffectingSpan() {
  override fun updateDrawState(tp: TextPaint) {
    tp.letterSpacing = letterSpacing
  }

  override fun updateMeasureState(tp: TextPaint) {
    tp.letterSpacing = letterSpacing
  }
}
