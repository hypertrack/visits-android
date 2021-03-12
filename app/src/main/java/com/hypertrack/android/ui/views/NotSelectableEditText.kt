package com.hypertrack.android.ui.views

import android.content.Context
import android.text.method.KeyListener
import android.util.AttributeSet
import android.widget.EditText

class NotSelectableEditText : androidx.appcompat.widget.AppCompatEditText {

    private var savedKeyListener: KeyListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    public override fun onSelectionChanged(start: Int, end: Int) {
        val text = this.text
        if (text != null) {
            if (start != text.length || end != text.length) {
                setSelection(text.length, text.length)
            }
        }
        super.onSelectionChanged(start, end)
    }

    fun setDisabled(disabled: Boolean) {
        isFocusableInTouchMode = !disabled
        isFocusable = !disabled
        isEnabled = !disabled

        if (disabled) {
            savedKeyListener = keyListener
            keyListener = null
        } else {
            keyListener = savedKeyListener
        }
    }
}