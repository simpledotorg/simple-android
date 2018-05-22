package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

@Entity(
    primaryKeys = ["patientUuid", "mobileNumberUuid"],
    foreignKeys = [
      ForeignKey(
          entity = Patient::class,
          parentColumns = ["uuid"],
          childColumns = ["patientUuid"]),
      ForeignKey(
          entity = PatientMobileNumber::class,
          parentColumns = ["uuid"],
          childColumns = ["mobileNumberUuid"])
    ],
    indices = [
      (Index("patientUuid", unique = true)),
      (Index("mobileNumberUuid", unique = true))
    ]
)
data class PatientAndMobileNumberJoin(
    val patientUuid: String,
    val mobileNumberUuid: String
)
