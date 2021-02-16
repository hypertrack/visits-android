package com.hypertrack.android.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MultipleClickListener(val times: Int, val listener: (View) -> Unit): View.OnClickListener {
    private var lastClickTime = System.currentTimeMillis()
    private var timesClicked = 0

    override fun onClick(view: View) {
        if(System.currentTimeMillis() - lastClickTime > 1000) {
            timesClicked = 0
        }
        timesClicked++
        lastClickTime = System.currentTimeMillis()
        if(timesClicked >= times) {
            timesClicked = 0
            listener.invoke(view)
        }
    }
}

abstract class SimpleTextWatcher : TextWatcher {
    open fun afterChanged(text: String) {}

    override fun afterTextChanged(s: Editable?) {
        afterChanged((s ?: "").toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}


abstract class SimpleOnSelectedItemListener : AdapterView.OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onItemSelected(position, parent!!.adapter.getItem(position))
    }

    abstract fun onItemSelected(position: Int, item: Any)
}

fun View.setMargin(
    top: Int? = null,
    bottom: Int? = null,
    left: Int? = null,
    right: Int? = null
) {
    when (layoutParams) {
        is FrameLayout.LayoutParams -> {
            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                top?.let { topMargin = it }
                bottom?.let { bottomMargin = it }
                left?.let { leftMargin = it }
                right?.let { rightMargin = it }
            }
        }
        is LinearLayout.LayoutParams -> {
            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                top?.let { topMargin = it }
                bottom?.let { bottomMargin = it }
                left?.let { leftMargin = it }
                right?.let { rightMargin = it }
            }
        }
        is ConstraintLayout.LayoutParams -> {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                top?.let { topMargin = it }
                bottom?.let { bottomMargin = it }
                left?.let { leftMargin = it }
                right?.let { rightMargin = it }
            }
        }
        else -> throw  IllegalStateException(layoutParams::class.java.simpleName)
    }
}

fun View.setPaddingValue(top: Int? = null, bottom: Int? = null) {
    setPadding(
        paddingLeft,
        top ?: paddingTop,
        paddingRight,
        bottom ?: paddingBottom
    )
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

fun View.applyLayoutParams(action: (ViewGroup.LayoutParams) -> Unit) {
    layoutParams = (layoutParams as ViewGroup.LayoutParams).apply {
        action.invoke(this)
    }
}

fun <T : ViewGroup.LayoutParams> View.applyLayoutParamsTyped(action: (T) -> Unit) {
    layoutParams = (layoutParams as T).apply {
        action.invoke(this)
    }
}

@Deprecated("replace with doOnLayout")
fun View.afterLayout(listener: (View) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            listener.invoke(this@afterLayout)
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun RecyclerView.setLinearLayoutManager(context: Context) {
    layoutManager = LinearLayoutManager(context)
}

fun View.ifVisible(callback: (View) -> Unit) {
    ifVisibleElse(callback, {})
}

fun View.ifNotVisible(callback: (View) -> Unit) {
    ifVisibleElse({}, callback)
}

fun View.ifVisibleElse(callback: (View) -> Unit, elseCallback: (View) -> Unit) {
    if(isVisible) {
        callback.invoke(this)
    } else {
        elseCallback.invoke(this)
    }
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