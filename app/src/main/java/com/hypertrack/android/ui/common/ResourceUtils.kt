package com.hypertrack.android.ui.common

import com.hypertrack.android.utils.MyApplication

fun Int.stringFromResource(): String {
    return MyApplication.context.getString(this)
}