package com.hypertrack.android.ui.views

import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.hypertrack.android.ui.common.SimpleTextWatcher
import com.hypertrack.android.ui.common.hide
import com.hypertrack.android.ui.common.show
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.view_verification_code_item.view.*


open class VerificationCodeView : FrameLayout {

    var codeLength: Int = 4
    private var _code: String = ""
    var code: String
        get() = _code
        set(value) {
            etCode.setText(value)
        }
    private lateinit var itemsView: LinearLayout
    lateinit var etCode: NotSelectableEditText
    var listener: VerificationCodeListener? = null

    val isCodeComplete: Boolean
        get() = _code.length == codeLength

    /*var disabled: Boolean = false
        set(value) {
            field = value
            if(value) {
                etCode.setRawInputType(InputType.TYPE_NULL)
            } else {
                etCode.setRawInputType(defaultInputType)
            }
        }*/

    open val codeItemLayout = R.layout.view_verification_code_item

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView)
        codeLength = typedArray.getInteger(R.styleable.VerificationCodeView_code_length, 4)
        typedArray.recycle()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView)
        codeLength = typedArray.getInteger(R.styleable.VerificationCodeView_code_length, 4)
        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val inflater = LayoutInflater.from(context);
        val rootView = inflater.inflate(R.layout.view_verification_code, this, true) as FrameLayout
        itemsView = rootView.findViewById(R.id.verificationCodeView_lItems)
        etCode = rootView.findViewById(R.id.verificationCodeView_etCode)
        etCode.filters = arrayOf(
            InputFilter.LengthFilter(codeLength)
        )
        etCode.setRawInputType(defaultInputType)

        etCode.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterChanged(text: String) {
                _code = text
                displayCode(code)
                listener?.onCodeChanged(code, isCodeComplete)
            }
        })
        etCode.setOnEditorActionListener { v, actionId, event ->
            if (isDoneAction(actionId, event)) {
                listener?.onEnterPressed(isCodeComplete)
            }
            true
        }

        for (i in 0 until codeLength) {
            inflater.inflate(codeItemLayout, itemsView, true)
        }

        displayCode("")
    }

    fun displayCode(code: String) {
        for (i in code.indices) {
            val itemRoot = (itemsView.getChildAt(i) as ViewGroup)
            itemRoot.isEnabled = true
            (itemRoot.getChildAt(0) as TextView).text = mapCodeSymbol(code.substring(i, i + 1))
            itemRoot.verificationCodeView_ivCursor.hide()
        }
        for (i in code.length until codeLength) {
            val itemRoot = (itemsView.getChildAt(i) as ViewGroup)
            itemRoot.isEnabled = false
            (itemRoot.getChildAt(0) as TextView).text = ""

            if (i == code.length) {
                itemRoot.verificationCodeView_ivCursor.show()
            } else {
                itemRoot.verificationCodeView_ivCursor.hide()
            }
        }
    }

    fun setDisabled(disabled: Boolean) {
        etCode.setDisabled(disabled)
    }

    open fun mapCodeSymbol(symbol: String): String = symbol.toUpperCase()

    //    open val defaultInputType = InputType.TYPE_CLASS_TEXT
    open val defaultInputType = InputType.TYPE_CLASS_NUMBER

    interface VerificationCodeListener {
        fun onCodeChanged(code: String, complete: Boolean)
        fun onEnterPressed(complete: Boolean)
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return etCode.requestFocus(direction, previouslyFocusedRect)
    }

    private fun isDoneAction(actionId: Int, event: KeyEvent?): Boolean {
        return actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_ACTION_NEXT
                || (event?.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER)
    }

    companion object {
        const val BLINK_TIMEOUT = 500
    }
}