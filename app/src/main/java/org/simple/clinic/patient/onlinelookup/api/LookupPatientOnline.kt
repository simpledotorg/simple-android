package org.simple.clinic.patient.onlinelookup.api

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.ForPatientSync
import org.simple.clinic.patient.sync.ForPatientSync.Type.RecordRetentionFallbackDuration
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.util.UtcClock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class LookupPatientOnline @Inject constructor(
    private val patientSyncApi: PatientSyncApi,
    private val clock: UtcClock,
    @ForPatientSync(RecordRetentionFallbackDuration) private val fallbackRecordRetentionDuration: Duration
) {

  fun lookupWithIdentifier(identifier: String): Result {
    val response = patientSyncApi.lookup(PatientOnlineLookupRequest(identifier)).execute().body()!!

    val medicalRecords = response.patients.map(::convertResponseToMedicalRecord)

    return Result.Found(medicalRecords)
  }

  private fun convertResponseToMedicalRecord(
      response: CompleteMedicalRecordPayload
  ): CompleteMedicalRecord {
    val age = if (response.age != null) Age(response.age, response.ageUpdatedAt!!) else null
    val retainUntil = response.retention.computeRetainUntilTimestamp(
        instant = Instant.now(clock),
        fallbackRetentionDuration = fallbackRecordRetentionDuration
    )

    val phoneNumbers = response
        .phoneNumbers
        ?.map { payload ->
          payload.toDatabaseModel(response.id)
        } ?: emptyList()

    val businessIds = response
        .businessIds
        .map { payload ->
          payload.toDatabaseModel(response.id)
        }

    val patientProfile = PatientProfile(
        patient = Patient(
            uuid = response.id,
            fullName = response.fullName,
            gender = response.gender,
            dateOfBirth = response.dateOfBirth,
            age = age,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
            deletedAt = response.deletedAt,
            addressUuid = response.address.uuid,
            status = response.status,
            recordedAt = response.recordedAt,
            syncStatus = SyncStatus.DONE,
            reminderConsent = response.reminderConsent,
            deletedReason = response.deletedReason,
            registeredFacilityId = response.registeredFacilityId,
            assignedFacilityId = response.assignedFacilityId,
            retainUntil = retainUntil
        ),
        address = response.address.toDatabaseModel(),
        phoneNumbers = phoneNumbers,
        businessIds = businessIds
    )

    val medicalHistory = if (response.medicalHistory != null) {
      MedicalHistory(
          uuid = response.medicalHistory.uuid,
          patientUuid = response.medicalHistory.patientUuid,
          diagnosedWithHypertension = response.medicalHistory.hasHypertension ?: Answer.Unanswered,
          isOnHypertensionTreatment = response.medicalHistory.isOnTreatmentForHypertension,
          hasHadHeartAttack = response.medicalHistory.hasHadHeartAttack,
          hasHadStroke = response.medicalHistory.hasHadStroke,
          hasHadKidneyDisease = response.medicalHistory.hasHadKidneyDisease,
          diagnosedWithDiabetes = response.medicalHistory.hasDiabetes,
          syncStatus = SyncStatus.DONE,
          createdAt = response.medicalHistory.createdAt,
          updatedAt = response.medicalHistory.updatedAt,
          deletedAt = response.medicalHistory.deletedAt
      )
    } else {
      null
    }

    return CompleteMedicalRecord(
        patient = patientProfile,
        medicalHistory = medicalHistory,
        appointments = emptyList(),
        bloodSugars = emptyList(),
        bloodPressures = emptyList(),
        prescribedDrugs = emptyList()
    )
  }

  sealed class Result {
    data class Found(val medicalRecords: List<CompleteMedicalRecord>) : Result()
  }
}
