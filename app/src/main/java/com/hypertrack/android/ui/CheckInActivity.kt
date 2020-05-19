package com.hypertrack.android.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.hypertrack.android.navigateTo
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.view_models.CheckInViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_checkin_screen.*


class CheckInActivity : ProgressDialogActivity() {

    private val checkInModel: CheckInViewModel by viewModels {
        Injector.provideCheckinViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin_screen)

        etDriverId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {checkInModel.onTextChanged(it)}
            }
        })

        btnCheckIn.setOnClickListener {
            checkInModel.onLoginClick(etDriverId.text)
            btnCheckIn.isEnabled = false
        }

        checkInModel.enableCheckIn
            .observe(this, Observer { enable ->
                btnCheckIn.isEnabled = enable
                btnCheckIn.setBackgroundColor(getColor(if (enable) R.color.colorBlack else R.color.colorBtnDisable))
            })

        checkInModel.destination
            .observe(this, Observer { destination -> navigateTo(destination) })

        checkInModel.showProgresss.observe(this, Observer {show ->
            if (show) showProgress() else dismissProgress()
        })
    }

    companion object { const val TAG = "CheckInAct" }

}