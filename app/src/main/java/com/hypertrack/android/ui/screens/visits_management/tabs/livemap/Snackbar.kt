package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class Snackbar private constructor(
    parent: ViewGroup,
    content: View,
    contentViewCallback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<Snackbar?>(parent, content, contentViewCallback) {
    private val accessibilityManager = parent.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    private var hasAction = false
    private val callback: BaseCallback<Snackbar>? = null
    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun isShown(): Boolean {
        return super.isShown()
    }

    fun setAction(listener: View.OnClickListener?): Snackbar {
        val contentLayout = view.getChildAt(0) as SnackbarContentLayout
        val view: View = contentLayout.actionView
        if (listener != null) {
            hasAction = true
            view.visibility = View.VISIBLE
            view.setOnClickListener(listener)
        } else {
            view.visibility = View.GONE
            view.setOnClickListener(null)
            hasAction = false
        }
        return this
    }

    fun setAction(viewId: Int, listener: View.OnClickListener?): Snackbar {
        val contentLayout = view.getChildAt(0) as SnackbarContentLayout
        val view = contentLayout.findViewById<View>(viewId)
        if (view != null) {
            if (listener != null) {
                view.visibility = View.VISIBLE
                view.setOnClickListener(listener)
            } else {
                view.visibility = View.GONE
                view.setOnClickListener(null)
            }
        }
        return this
    }

    class Callback : BaseCallback<Snackbar?>() {
        override fun onShown(sb: Snackbar?) {}
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {}

        companion object {
            const val DISMISS_EVENT_SWIPE = 0
            const val DISMISS_EVENT_ACTION = 1
            const val DISMISS_EVENT_TIMEOUT = 2
            const val DISMISS_EVENT_MANUAL = 3
            const val DISMISS_EVENT_CONSECUTIVE = 4
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntRange(from = 1L)
    annotation class Duration
    companion object {
        const val LENGTH_INDEFINITE = -2
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0
        fun make(view: View, layoutResourceId: Int, duration: Int): Snackbar {
            val parent = findSuitableParent(view)
            return if (parent == null) {
                throw IllegalArgumentException("No suitable parent found from the given view. Please provide a valid view.")
            } else {
                val inflater = LayoutInflater.from(parent.context)
                val content = inflater.inflate(
                    layoutResourceId,
                    parent,
                    false
                ) as SnackbarContentLayout
                val snackbar = Snackbar(
                    parent,
                    content,
                    content
                )
                snackbar.duration = duration
                snackbar
            }
        }

        private fun findSuitableParent(view: View): ViewGroup? {
            var view: View? = view
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    return view
                }
                if (view is FrameLayout) {
                    fallback = view
                }
                if (view != null) {
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            return fallback
        }
    }

    init {
        getView().background = null
    }
}