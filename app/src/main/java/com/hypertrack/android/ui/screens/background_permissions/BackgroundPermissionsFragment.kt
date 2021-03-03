package com.hypertrack.android.ui.screens.background_permissions

import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseFragment
import com.hypertrack.android.ui.common.hide
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_background_permission.*

class BackgroundPermissionsFragment :
    BaseFragment<MainActivity>(R.layout.fragment_background_permission) {

    private val vm: BackgroundPermissionsViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hint = getString(R.string.background_location_permission_option_hint)
            val sb =
                SpannableStringBuilder(hint + "\n" + MyApplication.context.packageManager.backgroundPermissionOptionLabel)

            sb.setSpan(
                StyleSpan(Typeface.BOLD),
                hint.length,
                sb.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            ); // make first 4 characters Bold
            tvOptionHint.text = sb
        } else {
            tvOptionHint.hide()
        }

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        btnContinue.setOnClickListener {
            vm.onAllowClick(mainActivity())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        vm.onPermissionResult(mainActivity())
    }
}