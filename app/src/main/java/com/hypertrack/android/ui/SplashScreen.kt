package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hypertrack.android.utils.MyPreferences

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigateToDestination()

        finish()
    }

    private fun navigateToDestination() {
        MyPreferences(this@SplashScreen, Gson()).getDriverValue()?.let {
            startActivity(Intent(this@SplashScreen, ListActivity::class.java))
            return
        }
        startActivity(Intent(this@SplashScreen, CheckInActivity::class.java))
    }
}