package org.simple.clinic.appupdate

data class UpdateInfo(
    val availableVersionCode: Int,
    val isUpdateAvailable: Boolean,
    val isFlexibleUpdateType: Boolean
)
