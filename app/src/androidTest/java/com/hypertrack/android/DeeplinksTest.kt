package com.hypertrack.android

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import com.hypertrack.android.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep


class DeeplinksTest {

    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .addCategory("android.intent.category.BROWSABLE")
            //todo team url
        .setData(Uri.parse("https://hypertrack-logistics.app.link/031c7ac6fe3bbe50ed3603aa69418862246f4cd9a01163464a9219352ac4bf87"))

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>(intent)

    @Test
    fun myTest() {
        val scenario = activityRule.scenario
        sleep(10000)

        // Your test code goes here.
    }

}