package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.ContentViewCallback
import com.hypertrack.logistics.android.github.R

class SnackbarContentLayout : FrameLayout, ContentViewCallback {
    lateinit var contentView: View
        private set
    lateinit var actionView: Button
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        contentView = findViewById(R.id.ht_snackbar_content)
        actionView = findViewById(R.id.ht_action)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        contentView.alpha = 0.0f
        contentView
            .animate()
            .alpha(1.0f)
            .setDuration(duration.toLong())
            .setStartDelay(delay.toLong())
            .start()
        if (actionView.visibility == VISIBLE) {
            actionView.alpha = 0.0f
            actionView
                .animate()
                .alpha(1.0f)
                .setDuration(duration.toLong())
                .setStartDelay(delay.toLong())
                .start()
        }
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        contentView.alpha = 1.0f
        contentView
            .animate()
            .alpha(0.0f)
            .setDuration(duration.toLong())
            .setStartDelay(delay.toLong())
            .start()
        if (actionView.visibility == VISIBLE) {
            actionView.alpha = 1.0f
            actionView
                .animate()
                .alpha(0.0f)
                .setDuration(duration.toLong())
                .setStartDelay(delay.toLong())
                .start()
        }
    }
}