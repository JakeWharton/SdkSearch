package com.jakewharton.sdksearch.ui

import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.style.StrikethroughSpan
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
  private val onShare: (Item) -> Unit
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
    else -> false
  }

  fun setItem(item: Item) {
    this.item = item

    val packageAcronym = item.package_()
        .split('.')
        .joinToString(".") { if (it.matches(VERSION_PACKAGE)) it else it.first().toString() }
    if (item.deprecated()) {
      text.text = buildSpannedString {
        append(packageAcronym)
        append('.')
        inSpan(StrikethroughSpan()) {
          append(item.class_())
        }
      }
    } else {
      text.text = "$packageAcronym.${item.class_()}"
    }
  }

  companion object {
    private val VERSION_PACKAGE = """v\d+""".toRegex()
  }
}
