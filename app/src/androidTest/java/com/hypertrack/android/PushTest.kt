package com.hypertrack.android

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import com.hypertrack.android.ui.MainActivity
import org.junit.Rule

class PushTest {

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
        action = Intent.ACTION_SYNC
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>(intent)

//    @Test
    fun openVisitsListOnPushReceived() {
        //todo
    }

}