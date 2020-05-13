package com.hypertrack.android.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.hypertrack.android.navigateTo
import com.hypertrack.android.view_models.CheckInViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_checkin_screen.*


class CheckInActivity : AppCompatActivity() {

    private val checkInModel: CheckInViewModel by viewModels()

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
            .observe(this, Observer { enable -> btnCheckIn.isEnabled = enable })

        checkInModel.destination
            .observe(this, Observer { destination -> navigateTo(destination) })
    }

    companion object { const val TAG = "CheckInAct" }

}