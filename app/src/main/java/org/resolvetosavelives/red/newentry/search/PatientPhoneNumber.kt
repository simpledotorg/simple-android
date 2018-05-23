package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant

@Entity
data class PatientPhoneNumber(
    @PrimaryKey
    val uuid: String,

    val patientUuid: String,

    val number: String,

    val phoneType: PatientPhoneNumberType,

    val active: Boolean,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncPending: Boolean
)
