package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitType
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
    fun itShouldReturnEmptyListIfNoVisitsSaved() {
        val visits = myPreferences.restoreVisits()
        assertTrue(visits.isEmpty())
    }

    @Test
    fun crudVisits() {
        val visitsExpected = listOf(
            Visit(
                _id="42",
                visitType = VisitType.LOCAL
            ),
            Visit(
                _id = "24",
                completedAt = "2020-02-02T20:20:02.020Z",
                visitType = VisitType.GEOFENCE
            )
        )
        myPreferences.saveVisits(visitsExpected)
        val visitsGot = myPreferences.restoreVisits()
        assertEquals(visitsExpected, visitsGot)
    }
}