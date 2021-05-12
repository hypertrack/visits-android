package com.hypertrack.android.ui.base

import android.app.Dialog
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R

open class ProgressDialogFragment(layoutId: Int) : BaseFragment<MainActivity>(layoutId) {

    private val dialog by lazy { AnimatedDialog(requireContext()) }

    protected fun showProgress() = dialog.show()

    protected fun dismissProgress() = dialog.dismiss()

    override fun onLeave() {
        super.onLeave()
        dismissProgress()
    }

    companion object {
        const val TRIP_NOTIFICATION_ID = 1
        const val SYNC_NOTIFICATION_ID = 4242
    }

}

class AnimatedDialog(context: Context) : Dialog(context, R.style.LoaderDialog) {
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