package com.hypertrack.android.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R


@SuppressLint("Registered")
open class ProgressDialogActivity : AppCompatActivity() {


    private val dialog by lazy {  AnimatedDialog(this) }

    protected fun showProgress() = dialog.show()

    protected fun dismissProgress() = dialog.dismiss()

    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    protected fun showSyncNotification() {
        @Suppress("DEPRECATION", "suggested API isn't available before Oreo")
        val notification = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    Notification.Builder(this, MyApplication.CHANNEL_ID)
                else
                    Notification.Builder(this).setPriority(Notification.PRIORITY_LOW)
                )
            .setContentText("Refreshing Visits")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }


    protected fun dismissSyncNotification() {
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }

    companion object { const val SYNC_NOTIFICATION_ID = 4242 }

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