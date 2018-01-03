package com.jakewharton.sdksearch.ui

import android.graphics.Typeface.BOLD
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
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

internal class ItemViewHolder(
  private val root: View,
  private val onClick: (Item) -> Unit,
  private val onCopy: (Item) -> Unit,
  private val onShare: (Item) -> Unit,
  private val onSource: (Item) -> Unit
) : ViewHolder(root), OnClickListener, OnMenuItemClickListener {
  private val text: TextView = root.findViewById(R.id.text)
  private val overflow: View = root.findViewById(R.id.more_options)

  private val popup by lazy(NONE) {
    val window = PopupMenu(root.context, overflow)
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

    val packageAcronym = item.package_()
        .split('.')
        .joinToString(".") { if (it.matches(VERSION_PACKAGE)) it else it.first().toString() }

    val className = SpannableString(item.class_())
    val start = item.class_().indexOf(query, ignoreCase = true)
    className.setSpan(StyleSpan(BOLD), start, start + query.length, SPAN_INCLUSIVE_EXCLUSIVE)

    text.text = buildSpannedString {
      append(packageAcronym)
      append('.')

      if (item.deprecated()) {
        inSpan(StrikethroughSpan()) {
          append(className)
        }
      } else {
        append(className)
      }
    }
  }

  companion object {
    private val VERSION_PACKAGE = """v\d+""".toRegex()
  }
}
