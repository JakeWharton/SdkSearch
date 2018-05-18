package com.jakewharton.sdksearch.search.ui.util

import android.content.Context
import android.view.LayoutInflater

internal inline val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)
