package org.simple.clinic.storage.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier

fun SupportSQLiteDatabase.savePatientProfile(patientProfile: PatientProfile) {
  savePatientAddress(patientProfile, this)
  savePatient(patientProfile, this)
  savePatientBusinessIds(patientProfile, this)
}

private fun savePatientAddress(patientProfile: PatientProfile, database: SupportSQLiteDatabase) {
  with(patientProfile.address) {
    database.insert(
        "PatientAddress",
        mapOf(
            "uuid" to uuid,
            "streetAddress" to streetAddress,
            "colonyOrVillage" to colonyOrVillage,
            "zone" to zone,
            "district" to district,
            "state" to state,
            "country" to country,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deletedAt" to deletedAt
        )
    )
  }
}

private fun savePatient(patientProfile: PatientProfile, database: SupportSQLiteDatabase) {
  with(patientProfile.patient) {
    database.insert(
        "Patient",
        mapOf(
            "uuid" to uuid,
            "addressUuid" to addressUuid,
            "fullName" to fullName,
            "gender" to Gender.TypeAdapter.knownMappings[gender],
            "dateOfBirth" to dateOfBirth,
            "age_value" to age?.value,
            "age_updatedAt" to age?.updatedAt,
            "status" to PatientStatus.TypeAdapter.knownMappings[status],
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deletedAt" to deletedAt,
            "recordedAt" to recordedAt,
            "syncStatus" to syncStatus.name,
            "reminderConsent" to ReminderConsent.TypeAdapter.knownMappings[reminderConsent]
        )
    )
  }
}

private fun savePatientBusinessIds(
    patientProfile: PatientProfile,
    database: SupportSQLiteDatabase
) {
  patientProfile.businessIds.forEach { businessId ->
    with(businessId) {
      database.insert(
          "BusinessId",
          mapOf(
              "uuid" to uuid,
              "patientUuid" to patientUuid,
              "identifier" to identifier.value,
              "identifierType" to Identifier.IdentifierType.TypeAdapter.knownMappings[identifier.type],
              "metaVersion" to BusinessId.MetaDataVersion.TypeAdapter.knownMappings[metaDataVersion],
              "meta" to metaData,
              "createdAt" to createdAt,
              "updatedAt" to updatedAt,
              "deletedAt" to deletedAt
          )
      )
    }
  }
}

fun PatientProfile.withSyncStatus(syncStatus: SyncStatus): PatientProfile {
  return copy(patient = patient.copy(syncStatus = syncStatus))
}

fun SupportSQLiteDatabase.assertPatient(patient: Patient) {
  with(patient) {
    query(""" SELECT * FROM "Patient" WHERE "uuid" = '$uuid' """).use {
      it.moveToNext()
      it.assertValues(mapOf(
          "uuid" to uuid,
          "addressUuid" to addressUuid,
          "fullName" to fullName,
          "gender" to Gender.TypeAdapter.knownMappings[gender],
          "dateOfBirth" to dateOfBirth,
          "age_value" to age?.value,
          "age_updatedAt" to age?.updatedAt,
          "status" to PatientStatus.TypeAdapter.knownMappings[status],
          "createdAt" to createdAt,
          "updatedAt" to updatedAt,
          "deletedAt" to deletedAt,
          "recordedAt" to recordedAt,
          "syncStatus" to syncStatus.name,
          "reminderConsent" to ReminderConsent.TypeAdapter.knownMappings[reminderConsent]
      ))
    }
  }
}

fun SupportSQLiteDatabase.assertPatientAddress(patientAddress: PatientAddress) {
  with(patientAddress) {
    query(""" SELECT * FROM "PatientAddress" WHERE "uuid" = '$uuid' """).use {
      it.moveToNext()
      it.assertValues(mapOf(
          "uuid" to uuid,
          "streetAddress" to streetAddress,
          "colonyOrVillage" to colonyOrVillage,
          "zone" to zone,
          "district" to district,
          "state" to state,
          "country" to country,
          "createdAt" to createdAt,
          "updatedAt" to updatedAt,
          "deletedAt" to deletedAt
      ))
    }
  }
}
