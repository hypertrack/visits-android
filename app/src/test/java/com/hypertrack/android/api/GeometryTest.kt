package com.hypertrack.android.api

import com.hypertrack.android.utils.Injector
import org.junit.Assert.*
import org.junit.Test

class GeometryTest {
    @Test
    fun `it should deserialize polygons correctly`() {
        val polygonString = """{"type": "Polygon", "coordinates": [[[0.0, 0.0], [0.0, 0.1], [0.1, 0.0], [0.0, 0.0]]]}"""
        val moshi = Injector.getMoshi()
        val geometry = moshi.adapter(Geometry::class.java).fromJson(polygonString)
        assertNotNull(geometry)
        assertTrue(geometry is Polygon)

    }

    @Test
    fun `it should deserialize circular geofence correctly`() {

        val geofenceString = """{
            "geofence_id": "00001111-4047-4b28-a6ec-f934e870c425",
            "device_id": "F3DF6D4F-6A06-4883-8201-D767EA408030",
            "geofence_metadata": { "station": "A" },
            "created_at": "2020-02-02T20:20:02.020Z",
            "radius": 50,
            "geometry": {
                "type":"Point",
                "coordinates": [122.395223, 37.794763]
            }
        }"""
        val moshi = Injector.getMoshi()
        val geofence = moshi.adapter(Geofence::class.java).fromJson(geofenceString) ?: throw NullPointerException("Geofence should not be null")
        assertEquals("Point", geofence.type)

    }

    @Test
    fun `it should deserialize polygon geofence correctly`() {

        val geofenceString = """{
            "geofence_id": "00001111-4047-4b28-a6ec-f934e870c425",
            "device_id": "F3DF6D4F-6A06-4883-8201-D767EA408030",
            "created_at": "2020-02-02T20:20:02.020Z",
            "geofence_metadata": { "station": "B" },
            "geometry": {
                "type": "Polygon",
                "coordinates": [[
                    [-122.395237, 37.7947693], 
                    [-122.402321, 37.794374],
                    [-122.401371, 37.790205], 
                    [-122.389450, 37.791271], 
                    [-122.395237, 37.7947693]
                ]]
            }
        }"""
        val moshi = Injector.getMoshi()
        val geofence = moshi.adapter(Geofence::class.java).fromJson(geofenceString) ?: throw NullPointerException("Geofence should not be null")
        assertEquals("Polygon", geofence.type)
    }
}