package com.hypertrack.android

object TestMockData {

    val MOCK_TRIPS_JSON = """
            {
               "pagination_token" : null,
               "links" : {},
               "data" : [
                  {
                     "device_id" : "42",
                     "analytics" : {},
                     "completed_at" : null,
                     "status" : "active",
                     "metadata" : {
                        "reason" : "api-test"
                     },
                     "device_info" : {
                        "sdk_version" : "4.3.1",
                        "os_version" : "10"
                     },
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/0201c34b-53a8-4a4d-9c3d-cd28effd36e3?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmnt3kpsc"
                     },
                     "trip_id" : "0201c34b-53a8-4a4d-9c3d-cd28effd36e3",
                     "summary" : null,
                     "started_at" : "2020-09-15T07:40:32.643897Z"
                  },
                  {
                     "estimate" : {
                        "arrive_at" : "2020-09-15T09:28:38.176257Z",
                        "route" : {
                           "start_address" : "Sobornyi Ave, 196, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69000",
                           "end_address" : "Mykhayla Honcharenka Street, 9, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69061",
                           "duration" : 162,
                           "polyline" : {
                              "type" : "LineString",
                              "coordinates" : [
                                 [
                                    35.12218,
                                    47.84857
                                 ],
                                 [
                                    35.1237,
                                    47.84984
                                 ],
                                 [
                                    35.12407,
                                    47.85013
                                 ],
                                 [
                                    35.12415,
                                    47.85009
                                 ],
                                 [
                                    35.12512,
                                    47.84959
                                 ],
                                 [
                                    35.12544,
                                    47.8494
                                 ],
                                 [
                                    35.12483,
                                    47.84887
                                 ],
                                 [
                                    35.12445,
                                    47.84856
                                 ],
                                 [
                                    35.12416,
                                    47.84833
                                 ],
                                 [
                                    35.12363,
                                    47.84791
                                 ]
                              ]
                           },
                           "distance" : 569,
                           "remaining_duration" : 162
                        },
                        "reroutes_exceeded" : false
                     },
                     "eta_relevance_data" : {
                        "status" : true
                     },
                     "device_id" : "42",
                     "device_info" : {
                        "os_version" : "9",
                        "sdk_version" : "4.6.0-SNAPSHOT"
                     },
                     "status" : "active",
                     "completed_at" : null,
                     "analytics" : {},
                     "summary" : null,
                     "trip_id" : "4ee5713c-250e-4094-b6c6-7c7ae33717b6",
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/4ee5713c-250e-4094-b6c6-7c7ae33717b6?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntkgzcj"
                     },
                     "started_at" : "2020-09-15T07:54:02.305516Z",
                     "destination" : {
                        "geometry" : {
                           "type" : "Point",
                           "coordinates" : [
                              35.1235317438841,
                              47.847959440945
                           ]
                        },
                        "address" : "Mykhayla Honcharenka Street, 9, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69061",
                        "scheduled_at" : null,
                        "radius" : 30
                     }
                  },
                  {
                     "device_id" : "42",
                     "eta_relevance_data" : {
                        "status" : true
                     },
                     "estimate" : {
                        "arrive_at" : "2020-09-15T09:12:23.584444Z",
                        "route" : {
                           "polyline" : {
                              "type" : "LineString",
                              "coordinates" : [
                                 [
                                    -122.50384,
                                    37.761
                                 ],
                                 [
                                    -122.50393,
                                    37.76239
                                 ],
                                 [
                                    -122.50428,
                                    37.76237
                                 ],
                                 [
                                    -122.50457,
                                    37.76235
                                 ],
                                 [
                                    -122.50607,
                                    37.76229
                                 ],
                                 [
                                    -122.50822,
                                    37.7622
                                 ],
                                 [
                                    -122.50965,
                                    37.76213
                                 ],
                                 [
                                    -122.50966,
                                    37.76221
                                 ]
                              ]
                           },
                           "distance" : 668,
                           "remaining_duration" : 137,
                           "start_address" : "1374 44th Ave, San Francisco, CA 94122, USA",
                           "end_address" : "1300 Great Hwy, San Francisco, CA 94122, USA",
                           "duration" : 137
                        },
                        "reroutes_exceeded" : false
                     },
                     "completed_at" : null,
                     "analytics" : {},
                     "device_info" : {
                        "sdk_version" : "4.4.0",
                        "os_version" : "13.5.1"
                     },
                     "status" : "active",
                     "summary" : null,
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/6f6d89eb-6b0e-444f-bf32-8601d488c69b?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntdawy3"
                     },
                     "trip_id" : "6f6d89eb-6b0e-444f-bf32-8601d488c69b",
                     "destination" : {
                        "radius" : 30,
                        "scheduled_at" : null,
                        "address" : "1300 Great Hwy, San Francisco, CA 94122, USA",
                        "geometry" : {
                           "coordinates" : [
                              -122.509639,
                              37.762207
                           ],
                           "type" : "Point"
                        }
                     },
                     "started_at" : "2020-09-15T07:51:38.828420Z"
                  },
                  {
                     "device_id" : "42",
                     "completed_at" : null,
                     "analytics" : {},
                     "device_info" : {
                        "os_version" : "10",
                        "sdk_version" : "4.3.1"
                     },
                     "metadata" : {
                        "reason" : "api-test"
                     },
                     "status" : "active",
                     "summary" : null,
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/9b5c5fa6-4f77-4ed6-b03f-4e19c238b8f0?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntqv5qd"
                     },
                     "trip_id" : "9b5c5fa6-4f77-4ed6-b03f-4e19c238b8f0",
                     "started_at" : "2020-09-15T07:58:07.937579Z"
                  }
               ]
            }
            """.trimIndent()

    val MOCK_HISTORY_JSON = """
            {
               "markers" : [
                    {
                         "marker_id" : "8b6aeb0f-1a8f-4900-95ee-03755ba21015",
                         "data" : {
                            "value" : "inactive",
                            "end" : {
                               "recorded_at" : "2021-02-05T11:53:10.544Z",
                               "location" : {
                                  "geometry" : {
                                     "coordinates" : [ -122.397368, 37.792382 ],
                                     "type" : "Point"
                                  },
                                  "recorded_at" : "2021-02-05T11:53:10.544Z"
                               }
                            },
                            "start" : {
                               "recorded_at" : "2021-02-05T00:00:00+00:00",
                               "location" : {
                                  "geometry" : {
                                     "type" : "Point",
                                     "coordinates" : [ -122.397368, 37.792382 ]
                                  },
                                  "recorded_at" : "2021-02-05T11:53:10.544Z"
                               }
                            },
                            "duration" : 42791,
                            "reason" : "stopped_programmatically"
                         },
                         "type" : "device_status"
                     },
                     {
                         "data" : {
                            "metadata" : {
                               "type" : "Test geotag at 1612342206755"
                            },
                            "location" : {
                               "type" : "Point",
                               "coordinates" : [ -122.084, 37.421998, 5 ]
                            },
                            "recorded_at" : "2021-02-03T08:50:06.757Z"
                         },
                         "type" : "trip_marker",
                         "marker_id" : "b05df9e8-8f91-44eb-b01f-bacfa59b4349"
                    },
                    {
                        "marker_id" : "5eb13571-d3cc-494d-966e-1cc5759ba965",
                        "type" : "geofence",
                        "data" : {
                           "exit" : {
                              "location" : {
                                 "geometry" : null,
                                 "recorded_at" : "2021-02-05T12:18:20.986Z"
                              }
                           },
                           "duration" : 403,
                           "arrival" : {
                              "location" : {
                                 "geometry" : {
                                    "coordinates" : [-122.4249, 37.7599 ],
                                    "type" : "Point"
                                 },
                                 "recorded_at" : "2021-02-05T12:11:37.838Z"
                              }
                           },
                           "geofence" : {
                              "metadata" : {
                                 "name" : "Mission Dolores Park"
                              },
                              "geometry" : {
                                 "coordinates" : [
                                    -122.426366,
                                    37.761115
                                 ],
                                 "type" : "Point"
                              },
                              "geofence_id" : "8b63f7d3-4ba4-4dbf-b100-0c843445d5b2",
                              "radius" : 200
                           }
                        }
                     }
                ],
               "device_id" : "A24BA1B4-3B11-36F7-8DD7-15D97C3FD912",
               "completed_at" : "2021-02-05T22:00:00.000Z",
               "locations" : {
                  "coordinates" : [],
                  "type" : "LineString"
               },
            "started_at": "2021-02-16T22:00:00.000Z",
            "completed_at": "2021-02-17T22:00:00.000Z",
            "distance": 1007.0,
            "duration": 86400,
            "tracking_rate": 100.0,
            "inactive_reasons": [],
            "inactive_duration": 0,
            "active_duration": 158,
            "stop_duration": 0,
            "drive_duration": 158,
            "walk_duration": 0,
            "trips": 0,
            "geotags": 0,
            "geofences_visited": 0
            }
        """.trimIndent()

}