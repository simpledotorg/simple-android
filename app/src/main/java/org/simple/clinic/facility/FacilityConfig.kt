package org.simple.clinic.facility

import androidx.room.ColumnInfo

data class FacilityConfig(
    @ColumnInfo(name = "diabetesManagementEnabled")
    val diabetesManagementEnabled: Boolean
)
