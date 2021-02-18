package com.hypertrack.android.ui.common

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R

object SnackbarUtil {
    fun showErrorSnackbar(view: View, text: String?) {
        Snackbar.make(view, text.toString(), Snackbar.LENGTH_LONG).apply {
                    setAction(MyApplication.context.getString(R.string.close)) {
                        dismiss()
                    }
                    setActionTextColor(MyApplication.context.getColor(R.color.colorRed))
                }.show()
    }
}