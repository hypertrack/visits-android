package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.utils.MyPreferences

class SplashScreen : AppCompatActivity() {

    private var myPreferences: MyPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        myPreferences = MyPreferences(this@SplashScreen)

        if (!myPreferences?.getDriverValue()?.driver_id.isNullOrEmpty()) {
            startActivity(Intent(this@SplashScreen, ListActivity::class.java))

        } else {
            startActivity(Intent(this@SplashScreen, CheckInActivity::class.java))
        }

        finish()
    }
}