package com.hypertrack.android.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat

class CustomSwitch : SwitchCompat {

    private var listener: OnCheckedChangeListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
        super.setOnCheckedChangeListener(listener)
    }

    fun setStateWithoutTriggeringListener(isChecked: Boolean) {
        val l = listener
        setOnCheckedChangeListener(null)
        this.isChecked = isChecked
        setOnCheckedChangeListener(l)
    }
}