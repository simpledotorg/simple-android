package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

// TODO: find a better package for Patient and its related classes.

@Entity(
    foreignKeys = [
      ForeignKey(
          entity = PatientAddress::class,
          parentColumns = ["uuid"],
          childColumns = ["addressUuid"])
    ],
    indices = [
      Index("addressUuid", unique = true)
    ])
data class Patient(
    @PrimaryKey
    val uuid: String,

    val addressUuid: String,

    val phoneNumberUuid: String,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    val ageWhenCreated: Int?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncPending: Boolean
)
