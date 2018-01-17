package com.jakewharton.sdksearch.ui

import android.graphics.Typeface.BOLD
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableString
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
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.util.buildSpannedString
import com.jakewharton.sdksearch.util.inSpan
import kotlin.LazyThreadSafetyMode.NONE

internal typealias ItemHandler = (Item) -> Unit

internal class ItemViewHolder(
  private val root: View,
  private val onClick: ItemHandler,
  private val onCopy: ItemHandler,
  private val onShare: ItemHandler,
  private val onSource: ItemHandler
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
      onClick.invoke(item!!)
    }
  }

  override fun onMenuItemClick(menuItem: MenuItem) = when(menuItem.itemId) {
    R.id.copy -> {
      onCopy(item!!)
      true
    }
    R.id.share -> {
      onShare(item!!)
      true
    }
    R.id.source -> {
      onSource(item!!)
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

    packageNameText.text = buildSpannedString {
      item.packageName.split('.').forEachIndexed { index, part ->
        if (index > 0) {
          inSpan(LetterSpacingSpan(PERIOD_LETTER_SPACING)) {
              append('.')
          }
        }
        append(part)
      }
    }

    val className = SpannableString(item.className)
    val start = item.className.indexOf(query, ignoreCase = true)
    className.setSpan(StyleSpan(BOLD), start, start + query.length, SPAN_INCLUSIVE_EXCLUSIVE)

    val nestedClassSeparatorColor = classNameText.currentTextColor.withAlpha(0x8A)
    var dotIndex = item.className.indexOf('.')
    while (dotIndex >= 0) {
      className.setSpan(LetterSpacingSpan(PERIOD_LETTER_SPACING),
              dotIndex, dotIndex + 1, SPAN_INCLUSIVE_EXCLUSIVE)
      className.setSpan(ForegroundColorSpan(nestedClassSeparatorColor),
              dotIndex, dotIndex + 1, SPAN_INCLUSIVE_EXCLUSIVE)

      dotIndex = item.className.indexOf('.', dotIndex + 1)
    }

    classNameText.text = buildSpannedString {
      if (item.deprecated) {
        inSpan(StrikethroughSpan()) {
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
