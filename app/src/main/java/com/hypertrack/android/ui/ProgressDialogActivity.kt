package com.hypertrack.android.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.hypertrack.logistics.android.github.R


@SuppressLint("Registered")
open class ProgressDialogActivity : AppCompatActivity() {


    private val dialog by lazy {  AnimatedDialog(this) }

    protected fun showProgress() = dialog.show()

    protected fun dismissProgress() = dialog.dismiss()


}

class AnimatedDialog(context: Context): Dialog(context, R.style.LoaderDialog) {
    init {
        setContentView(R.layout.dialog_progress_bar)
    }
    private val animation = findViewById<LottieAnimationView>(R.id.loader)

    override fun show() {
        super.show()
        setCancelable(false)
        animation.playAnimation()
    }

    override fun dismiss() {
        animation.cancelAnimation()
        super.dismiss()
    }
}