package com.jakewharton.sdksearch.util

import android.view.View

inline var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(visible) { visibility = if (visible) View.VISIBLE else View.GONE }