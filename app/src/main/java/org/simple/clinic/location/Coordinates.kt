package org.simple.clinic.location

import androidx.room.ColumnInfo

data class Coordinates(

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double
)
