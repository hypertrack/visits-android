# Changelog

## [Unreleased]

## [4.11.0] - 2021-03-30
### Added
- Restricted geotag interface method, that creates geotag when device is within specified region and fails otherwise.

## [4.10.1] - 2021-03-19
### Fixed
- Fixed concurrency bug resulted in multiple outages being genrated

## [4.10.0] - 2021-02-22
### Added
- Dynamic publishable key change support (comes in handy for test/prod switching)
- Background Location permission was made optional (with a dedicated setter on SDK instance)
### Fixed
- Handler based scheduler replaced with coroutines as a workaround to a memory leek in OS APIs
- Fixed race conditions that could causes involuntary tracking stops
- Human readable explanation of process exit reason on Android 11
- Constant device id (was only on Oreo or later before)

## [4.9.0] - 2020-12-23
### Fixed
- `HypeTrackMessagingService` was removed to avoid requirement of overriding it instead of `FirebaseMessagingservice`.

## [4.8.0] - 2020-10-30
### Fixed
- Backend and local tracking state conflict fix via their timestampts comparison.
- Sparse locations after exits from stops are no longer appears.

## [4.7.0] - 2020-10-16
### Added
- Android 11 compatibility: SDK will ask for background location access permission on Android 11
and correctly detect location access restrictions, that caused by new permission policy.
### Fixed
- Fixed a bug when SDK had delay in increasing locations frequency after stops.
- Removed default large icon in tracking notification as per Material Design Guidelines
- Small icon is now also configurable through overridden resources.
Vector drawable with name `ic_hypertrack_notification` will be used as a default small notifiation icon.

## [4.6.0] - 2020-09-25
### Added
- Automatic sync on internal triggers (sdk init, publishable key set etc).
- SDK dynamic configuration from remote.
### Fixed
- Steps counter reporting total instead of increment
- Invalid locations client-side check

## [4.5.4] - 2020-09-21
### Fixed
- Fixed a database migration bug that could result in inability of creating geotags

## [4.5.3] - 2020-07-27
### Fixed
- Bug that blocked metadata propagation from device to platform on Android 4.4 (API 19).

## [4.5.2] - 2020-07-24
### Changed
- Stability: switched from Parceable serialization to avoid deserialization issues on reboot.

## [4.5.1] - 2020-07-20
### Changed
- Device name and metadata changes are propagated to platform immediately.
- Missing permission error is only reported on tracking start.

## [4.5.0] - 2020-07-13
### Added
- Added logic to mark tracking segments where location updates weren't available.


## [4.4.1] - 2020-06-16
### Changed
- Custom markers were renamed to geotags

## [4.4.0] - 2020-06-16
### Added
- Expected location in custom Marker
- Enabled network payload compression

## [4.3.2] - 2020-05-10
### Changed
- Events batching on stops was disabled in fake location mode to ease testing

## [4.3.1] - 2020-05-05
### Added
- Added HyperTrackMessaging service to sdk manifest to ease the integration process.

## [4.3.0] - 2020-04-28
### Changed
- Custom markers (formerly known as trip markers or custom events) were made propagating errors on invalid payload, instead of throwing errors.
- Crash on notification click with unserializable Pending Intent fixed.
- Minor changes

## [4.1.3] - 2020-03-23
### Added
- Added changes to device 2 platform contract to improve disconnected devices detection consistency

## [4.1.2] - 2020-03-13
### Changed
- End of trial period handling logic changed to avoid unnecessary networking (retries etc.)
- Fixed a crash due to uninformative exception in underlying SQLite Db initialization call.

## [4.1.0] - 2020-02-29
### Changed
- Switched from grandcentrix's tray library to custom local sqlite storage implementation
- Firebase dependency version specified as required (was strict before)

## [4.0.0] - 2020-02-25
### Added
- Notification about 403 authentication error is propagated to SDK state observer.
### Removed
- Removed possibility to create a trip for this device directly from the sdk.

## [3.8.5] - 2020-02-16
### Changed
- Fixed recursion bug that led to battery drain

## [3.8.4] - 2020-02-14
### Changed
- Remote tracking intent is distinguished from sdk API intent and visible in dashboard

## [3.8.3] - 2020-01-24
### Changed
- Fixed a crash due to onNewToken call outside of looped thread
- Fixed a crash due to broken lifecycle invariants

## [3.8.0] - 2020-01-08
### Added
- Added possibility to create a trip for the device directly from SDK.
### Changed
 - Android 10 background data access issue fixed

## [3.7.0] - 2019-12-20
### Changed
- SDK startup time reduced due to programmatic detection of auth token expiration

## [3.6.0] - 2019-12-03
### Changed
- Excessive wake lock usage eliminated via switching from `AlarmManager` to `Handler` based tasks scheduling.

## [3.5.2] - 2019-11-14
### Changed
- Null device metadata bug fixed

## [3.5.0] - 2019-10-14
### Added
- New, instance centric, interface for SDK (old methods are kept, but marked deprecated).
- Possibility to configure persistent notification via config

## [3.4.6] - 2019-09-16
### Added
- Android 10 support

## [3.3.2] - 2019-08-13
### Changed
- Device ID generator changed to have publishable key among seeds, so device ids will be different
for the same physical device if sdk is initialized with different publishable Key (addresses
dev/prod credentials usage on the same device).

## [3.3.1] - 2019-08-08
### Changed
- Updated push notification feature to support token fetch during integration in app, that already received it from Firebase

## [3.4.3] - 2019-08-05
### Changed
- Fixed bugs in tracking observer functionality
- `isTracking` bug fixed.

## [3.4.2] - 2019-07-31
### Added
- Possibility to attach an observer, that is notified on SDK state changes
### Changed
- Displacement sensivity increased to finer tracking

## [3.3.0] - 2019-07-17
### Changed
- Trip markers replaced custom events.

## [3.2.0] - 2019-07-04
### Added
- Server to device communication support added. It is possible to start/stop tracking from platform.





