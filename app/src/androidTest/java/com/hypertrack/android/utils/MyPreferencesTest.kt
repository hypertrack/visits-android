package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import com.hypertrack.android.repository.Driver
import com.hypertrack.android.repository.Delivery
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
    fun crudDriver() {

        val driverId = "Kowalski"

        val driver = Driver(driverId)
        myPreferences.saveDriver(driver)
        val restoredDriver = myPreferences.getDriverValue()
        assertEquals(driverId, restoredDriver.driverId)
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

    @Test
    fun itShouldReturnEmptyListIfNoDeliveriesSaved() {
        val deliveries = myPreferences.restoreDeliveries()
        assertTrue(deliveries.isEmpty())
    }

    @Test
    fun crudDeliveries() {
        val deliveriesExpected = listOf(
            Delivery(
                "pending",
                "42"
            ),
            Delivery(
                "completed",
                "24",
                completedAt = "2020-02-02T20:20:02.020Z"
            )
        )
        myPreferences.saveDeliveries(deliveriesExpected)
        val deliveriesGot = myPreferences.restoreDeliveries()
        assertEquals(deliveriesExpected, deliveriesGot)
    }
}