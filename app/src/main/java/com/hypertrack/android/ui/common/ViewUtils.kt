package com.hypertrack.android.ui.common

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleTextWatcher : TextWatcher {
    open fun afterChanged(text: String) {}

    override fun afterTextChanged(s: Editable?) {
        afterChanged((s ?: "").toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}


fun List<View>.hide() {
    forEach {
        it.hide()
    }
}

fun List<View>.show() {
    forEach {
        it.show()
    }
}

fun RecyclerView.setLinearLayoutManager(context: Context) {
    layoutManager = LinearLayoutManager(context)
}

fun View.hide() {
    visibility = View.GONE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.setInvisibleState(value: Boolean) {
    visibility = if (value) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}

fun View.setGoneState(value: Boolean): View {
    visibility = if (value) {
        View.GONE
    } else {
        View.VISIBLE
    }
    return this
}

fun View.goneIfNull(value: Any?) {
    visibility = if (value == null) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

fun View.goneIfEmpty(value: List<Any>): View {
    visibility = if (value.isEmpty()) {
        View.GONE
    } else {
        View.VISIBLE
    }
    return this
}

fun View.showIfEmpty(value: List<Any>) {
    visibility = if (value.isNotEmpty()) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

fun TextView.textString(): String {
    return text.toString()
}

fun String.toView(textView: TextView) {
    textView.text = this
}

fun Int.toView(imageView: ImageView) {
    imageView.setImageResource(this)
}