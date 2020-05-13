package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import com.hypertrack.android.repository.Driver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class MyPreferencesTest {

    private lateinit var myPreferences: MyPreferences

    @Before
    fun setUp() {
        myPreferences =
            MyPreferences(InstrumentationRegistry.getInstrumentation().targetContext, Gson())
        myPreferences.clearPreferences()
    }

    @Test
    fun itShouldReturnDriverWithoutIdIfNoDriverSaved() {
        val driver = myPreferences.getDriverValue()
        assertTrue(driver.driverId.isEmpty())
    }

    @Test
    fun itShouldReturnNullIfNoRepoSaved() {
        assertNull(myPreferences.restoreRepository())
    }

    @Test
    fun crudDriverWithoutDeliveries() {

        val driver_id = "Kowalski"

        val driver = Driver(driver_id)
        myPreferences.saveDriver(driver)
        val restoredDriver = myPreferences.getDriverValue()
        assertEquals(driver_id, restoredDriver.driverId)
        myPreferences.clearPreferences()
    }

    @Test
    fun crudDriverWithAccessTokenRepo() {

        val token = "expired.jwt.token"
        val repo = BasicAuthAccessTokenRepository("localhost", "42", "fake-key", "", token)

        myPreferences.persistRepository(repo)

        val restoredRepo = myPreferences.restoreRepository()!!
        assertEquals(token, restoredRepo.getAccessToken())


        myPreferences.clearPreferences()
    }

}