package org.simple.clinic.appupdate

data class AppUpdateConfig(
    val inAppUpdateEnabled: Boolean,
    val differenceBetweenVersionsToNudge: Long
)
