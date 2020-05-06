package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.Geometry
import com.hypertrack.android.response.Delivery
import com.hypertrack.android.response.DriverModel
import junit.framework.Assert.*
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
    fun itShouldReturnNullIfNoDriverSaved() {
        assertNull(myPreferences.getDriverValue())
    }

    @Test
    fun crudDriverWithoutDeliveries() {

        val device_id = "device-42"
        val driver_id = "Kowalski"

        val driver = DriverModel(device_id, driver_id, emptyList())
        myPreferences.saveDriver(driver)
        val restoredDriver = myPreferences.getDriverValue()!!
        assertEquals(device_id, restoredDriver.device_id)
        assertEquals(driver_id, restoredDriver.driver_id)
        assertTrue(restoredDriver.deliveries.isEmpty())
        myPreferences.clearPreferences()
    }


    @Test
    fun crudDriverWithDeliveries() {

        val device_id = "device-42"
        val driver_id = "Kowalski"
        val deliveries = getDeliveries(device_id)

        val driver = DriverModel(device_id, driver_id, deliveries)
        myPreferences.saveDriver(driver)
        val restoredDriver = myPreferences.getDriverValue()!!
        assertEquals(device_id, restoredDriver.device_id)
        assertEquals(driver_id, restoredDriver.driver_id)
        val restoredDeliveries = restoredDriver.deliveries
        assertEquals(2, restoredDeliveries.size)

        val firstDelivery = restoredDeliveries.first()
        assertEquals("42-42", firstDelivery.id)
        val anotherDelivery = restoredDeliveries[1]
        assertEquals("42-43", anotherDelivery.id)
        
        myPreferences.clearPreferences()
    }

    private fun getDeliveries(device_id: String): List<Delivery> {
        return listOf(
            Delivery(
                Geofence(
                    false, "now", null, device_id, emptyList(),
                    "42-42", Geometry(listOf(42.0, 42.0), "Point"),
                    mapOf("testGeofence" to true), 30, false
                )
            ),
            Delivery(
                Geofence(
                    false, "2020-02-02T20:20:02.020Z", null, device_id, emptyList(),
                    "42-43", Geometry(listOf(43.0, 43.0), "Point"),
                    mapOf("testGeofence" to true), 40, false
                )
            )
        )
    }


}