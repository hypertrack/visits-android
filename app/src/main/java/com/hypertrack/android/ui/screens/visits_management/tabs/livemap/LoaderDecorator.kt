package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.app.Dialog
import android.content.Context
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.hypertrack.logistics.android.github.R

class LoaderDecorator(context: Context) : Dialog(context, R.style.LoaderDialog) {
    fun start() {
        show()
        (findViewById<View>(R.id.loader) as LottieAnimationView).playAnimation()
    }

    fun stop() {
        (findViewById<View>(R.id.loader) as LottieAnimationView).cancelAnimation()
        dismiss()
    }

    init { setContentView(R.layout.dialog_progress_bar) }
}