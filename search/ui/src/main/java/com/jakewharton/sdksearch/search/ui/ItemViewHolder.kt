package com.jakewharton.sdksearch.search.ui

import android.graphics.Typeface.BOLD
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.Spannable
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.TextView
import androidx.text.buildSpannedString
import androidx.text.inSpans
import androidx.text.toSpannable
import com.jakewharton.sdksearch.store.Item
import kotlin.LazyThreadSafetyMode.NONE

internal class ItemViewHolder(
  private val root: View,
  private val callback: ItemAdapter.Callback
) : ViewHolder(root), OnClickListener, OnMenuItemClickListener {
  private val packageNameText: TextView = root.findViewById(R.id.package_name)
  private val classNameText: TextView = root.findViewById(R.id.class_name)
  private val overflow: View = root.findViewById(R.id.more_options)

  private val popup by lazy(NONE) {
    val window = PopupMenu(root.context, overflow, Gravity.NO_GRAVITY,
            0, android.R.style.Widget_Material_PopupMenu_Overflow)
    overflow.setOnTouchListener(window.dragToOpenListener)
    window.menuInflater.inflate(R.menu.item, window.menu)
    window.setOnMenuItemClickListener(this)
    return@lazy window
  }

  init {
    root.setOnClickListener(this)
    overflow.setOnClickListener(this)
  }

  private var query: String? = null
  private var item: Item? = null

  override fun onClick(view: View) {
    if (view == overflow) {
      popup.show()
    } else {
      callback.onItemClicked(item!!)
    }
  }

  override fun onMenuItemClick(menuItem: MenuItem) = when(menuItem.itemId) {
    R.id.copy -> {
      callback.onItemCopied(item!!)
      true
    }
    R.id.share -> {
      callback.onItemShared(item!!)
      true
    }
    R.id.source -> {
      callback.onItemViewSource(item!!)
      true
    }
    else -> throw IllegalArgumentException("Unknown menu item: $menuItem")
  }

  fun updateQuery(query: String) {
    this.query = query
    render()
  }

  fun updateItem(item: Item) {
    this.item = item
    render()
  }

  fun setItem(query: String, item: Item) {
    this.query = query
    this.item = item
    render()
  }

  private fun render() {
    val query = this.query!!
    val item = this.item!!

    val letterSpacingSpan = LetterSpacingSpan(PERIOD_LETTER_SPACING)
    val nestedClassSeparatorColor = classNameText.currentTextColor.withAlpha(0x8A)
    val foregroundColorSpan = ForegroundColorSpan(nestedClassSeparatorColor)

    packageNameText.text = buildSpannedString {
      item.packageName.split('.').forEachIndexed { index, part ->
        if (index > 0) {
          inSpans(letterSpacingSpan) {
            append('.')
          }
        }
        append(part)
      }
    }

    val className = item.className.toSpannable()
    val start = item.className.indexOf(query, ignoreCase = true)
    className[start, start + query.length] = StyleSpan(BOLD)

    var dotIndex = item.className.indexOf('.')
    while (dotIndex >= 0) {
      className[dotIndex, dotIndex + 1] = letterSpacingSpan
      className[dotIndex, dotIndex + 1] = foregroundColorSpan

      dotIndex = item.className.indexOf('.', dotIndex + 1)
    }

    classNameText.text = buildSpannedString {
      if (item.deprecated) {
        inSpans(StrikethroughSpan()) {
          append(className)
        }
      } else {
        append(className)
      }
    }
  }

  companion object {
    // We make it 20% 'smaller'
    private const val PERIOD_LETTER_SPACING = -0.2f
  }
}

@Suppress("NOTHING_TO_INLINE") // Convenience alias to public API.
private inline fun Int.withAlpha(alpha: Int) = ColorUtils.setAlphaComponent(this, alpha)

@Suppress("NOTHING_TO_INLINE") // Convenience alias to public API.
private inline operator fun Spannable.set(start: Int, end: Int, span: Any) {
  setSpan(span, start, end, SPAN_INCLUSIVE_EXCLUSIVE)
}
