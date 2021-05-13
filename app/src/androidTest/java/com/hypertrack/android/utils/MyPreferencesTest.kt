package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.android.interactors.PhotoForUpload
import com.hypertrack.android.interactors.PhotoUploadingState
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus
import com.hypertrack.android.models.VisitType
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import com.hypertrack.android.repository.Driver
import com.hypertrack.android.repository.MyPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@Suppress("EXPERIMENTAL_API_USAGE")
class MyPreferencesTest {

    private lateinit var myPreferences: MyPreferences

    @Before
    fun setUp() {
        myPreferences =
            MyPreferences(
                InstrumentationRegistry.getInstrumentation().targetContext,
                Injector.getMoshi()
            )
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
                _id = "42",
                visitType = VisitType.LOCAL,
                _state = VisitStatus.VISITED
            ),
            Visit(
                _id = "24",
                completedAt = "2020-02-02T20:20:02.020Z",
                visitType = VisitType.TRIP,
                _state = VisitStatus.COMPLETED
            )
        )
        myPreferences.saveVisits(visitsExpected)
        val visitsGot = myPreferences.restoreVisits()
        assertEquals(visitsExpected, visitsGot)
    }

    @Test
    fun crudPhotoUploadQueue() {
        runBlocking {
            //todo test overwrite for equal ids?
            myPreferences.addToPhotosQueue(
                PhotoForUpload(
                    "1",
                    "path",
                    "thumb",
                    PhotoUploadingState.ERROR
                ),
            )
            myPreferences.addToPhotosQueue(
                PhotoForUpload(
                    "2",
                    "path",
                    "thumb",
                    PhotoUploadingState.ERROR
                ),
            )
            myPreferences.getPhotosQueue().toList().let {
                assertEquals(2, it.size)
                assertEquals("1", it[0].photoId)
                assertEquals(PhotoUploadingState.ERROR, it[0].state)
            }
            myPreferences.updatePhotoState("2", PhotoUploadingState.UPLOADED)
            myPreferences.updatePhotoState("1", PhotoUploadingState.NOT_UPLOADED)
            myPreferences.getPhotosQueue().toList().let {
                assertEquals(1, it.size)
                assertEquals(PhotoUploadingState.NOT_UPLOADED, it[0].state)
            }
        }
    }

}