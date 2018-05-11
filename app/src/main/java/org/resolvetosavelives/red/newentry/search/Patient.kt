package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

// TODO: find a better package for Patient and its related classes.
@Entity
data class Patient(
    @PrimaryKey
    val uuid: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "mobile_number")
    val mobileNumber: String
)
