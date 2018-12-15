package com.jakewharton.sdksearch.search.ui

import android.graphics.Typeface.BOLD
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
import androidx.core.graphics.ColorUtils
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.set
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jakewharton.sdksearch.store.item.Item
import kotlin.LazyThreadSafetyMode.NONE

internal class ItemResultViewHolder(
  private val root: View,
  private val callback: ItemResultAdapter.Callback
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

  private var item: Item? = null

  override fun onClick(view: View) {
    if (view == overflow) {
      popup.show()
    } else {
      callback.onItemClicked(item!!)
    }
  }

  override fun onMenuItemClick(menuItem: MenuItem) = when (menuItem.itemId) {
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

  fun update(itemResult: ItemResult) {
    val query = itemResult.query
    val item = itemResult.item
    this.item = item

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

  @Suppress("NOTHING_TO_INLINE") // Convenience alias to public API.
  private inline fun Int.withAlpha(alpha: Int) = ColorUtils.setAlphaComponent(this, alpha)

  companion object {
    // We make it 20% 'smaller'
    private const val PERIOD_LETTER_SPACING = -0.2f
  }
}
