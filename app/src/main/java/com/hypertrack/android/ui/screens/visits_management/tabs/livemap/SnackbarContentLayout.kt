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
    var contentView: View? = null
        private set
    var actionView: Button? = null
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
        contentView?.let {
            it.alpha = 0.0f
            it
                .animate()
                .alpha(1.0f)
                .setDuration(duration.toLong())
                .setStartDelay(delay.toLong())
                .start()
            actionView?.let { view ->
                if (view.visibility == VISIBLE) {
                    view.alpha = 0.0f
                    view
                        .animate()
                        .alpha(1.0f)
                        .setDuration(duration.toLong())
                        .setStartDelay(delay.toLong())
                        .start()
                }

            }
        }
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        contentView?.let {
            it.alpha = 1.0f
            it
            .animate()
            .alpha(0.0f)
            .setDuration(duration.toLong())
            .setStartDelay(delay.toLong())
            .start()

        }
        actionView?.let { btn ->
            if (btn.visibility == VISIBLE) {
                btn.alpha = 1.0f
                btn
                    .animate()
                    .alpha(0.0f)
                    .setDuration(duration.toLong())
                    .setStartDelay(delay.toLong())
                    .start()
            }

        }
    }
}