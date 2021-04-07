package com.hypertrack.android.ui.common

import android.location.Address

fun Address.toAddressString(): String {
    return (locality?.let { "$it, " } ?: "") + (thoroughfare ?: "${latitude}, ${longitude}")
}