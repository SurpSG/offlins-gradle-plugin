# Offlins Gradle plugin Changelog

## 0.6.1
- #64 Fixed dependency configuration on nested projects


## 0.6.0
- Added option to exclude classes from coverage report
- Now html report is disabled by default
- Now xml and csv reports have default names: report.xml and report.csv 


## 0.5.0
- Official support of Gradle 8.11 and 8.12
- Updated default JaCoCo version to `0.8.12`
- Updated kotlin dependencies to `1.9.20`
- Min supported Gradle version was changed from `6.1` to `8.11`
- #35 Fixed issue with `coverageReport` task when the task fails on latest gradle


## 0.4.0

- Updated default JaCoCo version to `0.8.12`
- Updated kotlin dependencies to `1.9.20`
- Official support of Gradle 8.7
- Min supported Gradle version was changed from `6.1` to `6.9.4`


## 0.3.0

- Support Gradle 8
- Min supported Gradle version was changed from `5.1` to `6.1`


## 0.2.1

- Fixed plugin failure when project has test tasks that use the same source set


## 0.2.0

- Now `Offlins` plugin collects coverage from all test tasks
- `coverageReport` task is assigned to `verification` group


## 0.1.0

### Added

- `instrumentClassesOffline` task that instruments `.class` files offline
- `assembleInstrumentedJar` task that creates `jar` from instrumented classes
- `coverageReport` task that builds coverage reports

