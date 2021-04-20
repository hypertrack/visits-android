# Changelog

## [0.6.11] - 2021-04-20
### Added
- Copy button for profile data

## [0.6.10] - 2021-04-19
### Fixed
- Crash on places screen fixed for OPPO devices

## [0.6.9] - 2021-04-16
### Added
- Route info in place visit details
- Copy Visit ID button at Visit details screen

### Changed
- Places list is now sorted by last visit

### Fixed
- Minor text fixes

## [0.6.8] - 2021-04-15
### Fixed
- Crash on visits list fixed

## [0.6.7] - 2021-04-13
### Fixed
- Crash in case if there no internet connection fixed

## [0.6.6] - 2021-04-13
### Fixed
- Visits list text changes
- Local visit UX changes and fixes

## [0.6.5] - 2021-04-09
### Fixed
- Mock locations were disallowed

## [0.6.4] - 2021-04-08
### Added
- Added Place visits timeline

### Changed
- Changed Select location UX

### Fixed
- Fixed missing street number in address when creating geofence

## [0.6.3] - 2021-04-07
### Added
- Added Place creation screen

## [0.6.2] - 2021-04-06
### Updated
- HyperTrack SDK was updated to v4.11.0

## [0.6.1] - 2021-03-31
- Manual Visit creation enabled by default
- Bugfix

## [0.6.0] - 2021-03-31
- Added Places tab
- Added Place details

## [0.5.3] - 2021-03-26
- Minor Sign Up changes

## [0.5.2] - 2021-03-18
- Fixed a bug that could lead to crash and null error snackbar on a map view.

## [0.5.1] - 2021-03-16
- Bug with map focusing on 0,0 coordinates when no history available fixed

## [0.5.0] - 2021-03-16
- Sign Up for HyperTrack without leaving the app.
- Interactive timeline to review your daily history.
- Minor UI improvements

## [0.4.0] - 2021-03-11
- We made the map view default screen
- Tracking now starts automatically for the very first app launch.
- Some labels were replaced with icons to preserve space and improve usability
- Geotags payload was made self-explanatory

## [0.3.1] - 2021-03-03
- Daily stats in the Summary tab
- Profile tab was added to explore the associated data
- You can attach multiple photos to each visit
- UI update to achieve better usability

## [0.2.13] - 2021-01-26
- Added whitelisting prompt for Xiaomi, OnePlus and alike.
- Visit completion events have their expected location attached.

## [0.2.12] - 2021-01-26
- Switched to efficient geofences API with markers included
- Replased Gson and Mockito with Moshi and MockK counterparts
- Visit click crash was fixed

## [0.2.11] - 2021-01-26
- Notification about device being deleted
- Non-blocking visits refresh
- Pull to refresh instead of button click
- Pick-Up button configurability via deeplink parameter.
- Local visit UI fixed.
- Bugfixes

## [0.2.10] - 2021-01-26
- HyperTrack SDK updated to v4.9.0
- Gson nullability issue fixed

## [0.2.9] - 2020-12-21
- Proof of Deliver photo can be attached to each visit
- Crashlytics integration
- Timeouts were incremented to 30 secs.

## [0.2.8] - 2020-12-15
- Auto check in for Trips and Geofences
- Updated visit state model to match Pick Up -> Check In -> Check Out / Cancel graph
- Daily history view added

## [0.2.7] - 2020-11-27
- Added login with HyperTrack Dashboard credentials
- Misc behavior changes

## [0.2.6-devpoc] - 2020-11-04
- Fixed issue with manual visits configuration been ignored

## [0.2.6] - 2020-10-30
- Added local visits and driver id configurability via deeplink

## [0.2.6-rc01] - 2020-10-27
- CheckIn/CheckOut is only available if configured in deeplinks
- Driver id can be passed as a deeplink parameter

## [0.2.5] - 2020-10-06
- Trip metadata entries, that have their keys starts with "ht_" prefix, aren't shown in customer notes.

## [0.2.4] - 2020-10-01
- Fixed a crash on network error.


