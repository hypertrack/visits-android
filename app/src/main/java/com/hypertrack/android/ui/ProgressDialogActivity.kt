package com.hypertrack.android.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.logistics.android.github.R


open class ProgressDialogActivity : AppCompatActivity() {


    private var dialog: Dialog? = null

    protected fun showProgress() {

        val newDialog = dialog ?: Dialog(this)
        newDialog.setCancelable(false)
        newDialog.setContentView(R.layout.dialog_progress_bar)
        newDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        newDialog.show()

        dialog = newDialog
    }

    protected fun dismissProgress() = dialog?.dismiss()


}