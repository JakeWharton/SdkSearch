package com.jakewharton.sdksearch.ui

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class LetterSpacingSpan(private val letterSpacing: Float = 0.0f) : MetricAffectingSpan() {
  override fun updateDrawState(tp: TextPaint) {
    tp.letterSpacing = letterSpacing
  }

  override fun updateMeasureState(tp: TextPaint) {
    tp.letterSpacing = letterSpacing
  }
}