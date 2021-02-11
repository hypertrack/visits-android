package com.hypertrack.android.ui

@Deprecated("")
class DriverIdInputActivity : ProgressDialogActivity() {

//    private val driverLoginModel: DriverLoginViewModel by viewModels {
//        MyApplication.injector.provideDriverLoginViewModelFactory(applicationContext)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_driver_id_input)
//
//        etDriverId.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                s?.let {driverLoginModel.onTextChanged(it)}
//            }
//        })
//
//        btnCheckIn.setOnClickListener {
//            driverLoginModel.onLoginClick(etDriverId.text)
//            btnCheckIn.isEnabled = false
//        }
//
//        driverLoginModel.enableCheckIn
//            .observe(this, { enable ->
//                btnCheckIn.isEnabled = enable
//                btnCheckIn.background = ContextCompat.getDrawable(this,
//                    if (enable) R.drawable.bg_button
//                    else R.drawable.bg_button_disabled
//                )
//            })
//
//        driverLoginModel.destination
//            .observe(this, { destination -> navigateTo(destination) })
//
//        driverLoginModel.showProgresss.observe(this, Observer { show ->
//            if (show) showProgress() else dismissProgress()
//        })
//
//        driverLoginModel.checkAutoLogin()
//    }
//
//    companion object { const val TAG = "LoginAct" }

}