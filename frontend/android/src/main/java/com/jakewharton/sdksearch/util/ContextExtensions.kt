package com.jakewharton.sdksearch.util

import android.content.Context
import android.view.LayoutInflater

inline val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)
