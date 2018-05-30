package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

@Entity(
    primaryKeys = ["patientUuid", "phoneNumberUuid"],
    foreignKeys = [
      ForeignKey(
          entity = Patient::class,
          parentColumns = ["uuid"],
          childColumns = ["patientUuid"]),
      ForeignKey(
          entity = PatientPhoneNumber::class,
          parentColumns = ["uuid"],
          childColumns = ["phoneNumberUuid"])
    ],
    indices = [
      (Index("patientUuid")),
      (Index("phoneNumberUuid"))
    ]
)
data class PatientWithPhoneNumber(
    val patientUuid: String,
    val phoneNumberUuid: String
)
