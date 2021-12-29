# App Database Pruning

Simple is an offline-first application, i.e, the critical features required for patient care are designed to work without needing online connectivity.

Because of this requirement, we store a large number of patients locally on the device so that operations are looking up a patient can be performed locally without needing internet connections, which can be unreliable.

However, this comes with the tradeoff that the performance of the application degrades as the number of patients being stored on the device grows. Because of this, we regularly run maintenance tasks on the local database to remove records that are either no longer relevant or which are deemed to be of low importance so that we can keep the performance of the application at an applicable level.

This document aims to record the conditions in which the database maintenance is run, and what are the factors that led to them being considered for deletion.

#### Types of pruning

There are two kinds of database prunes that we run.

1. [Deleting patients who are in a different block](#delete-records-from-a-different-block)
2. [Deleting records that are no longer necessary to keep on the device](#delete-unnecessary-records)

## Delete records from a different block

In order to reduce the number of patients we need to retain on a single device, we divide all the facilities in a particular region on the server into "sync groups". A user is allowed (legally) to access any patient record in the entire region, but on the device, we only retain the patients from within the sync group.

While it is definitely possible for a patient to visit a facility from a different sync group, we depend on the "online lookup" feature in order to reduce the chances of a patient getting re-registered as a duplicate.

Practically, this is triggered whenever a full data sync completes (see [LINK](https://github.com/simpledotorg/simple-android/blob/0d064b7af7650e237127795425e7c763fc7563f7/app/src/main/java/org/simple/clinic/sync/DataSync.kt#L175)). The reason we wait for a full data sync to complete before triggering this is because of the factors that go into deciding whether the patient record should be retained on this device or not. These conditions are:

- The patient is _either_ registered _or_ assigned to any facility in the user's current facility sync group, _OR_
- There is _at least_ one appointment which has been scheduled for a patient in the user's current facility sync group

In addition, in order to account for the case where a nurse might want to visit a facility in a different sync group, we skip running this pruning task until at least 24 hours have passed after a user has switched to a facility in a different sync group.

## Delete unnecessary records

These records are ones that are no longer necessary for functioning of the app and can be safely deleted from the device.

These are triggered at a regular interval (currently once a week) whenever the app starts (see [LINK](https://github.com/simpledotorg/simple-android/blob/0d064b7af7650e237127795425e7c763fc7563f7/app/src/main/java/org/simple/clinic/setup/SetupActivityEffectHandler.kt#L64)).

The records that we delete whenever this runs are those that satisfy the following conditions:
- Any record where the `is_deleted` property is _NOT_ `null`
- Any patient fetched via online lookup which has exceeded the local retention timestamp (sent from the server side)
- Any appointment which is either `cancelled` or `visited` since the app itself only needs to work with `scheduled` appointments