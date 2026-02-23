package org.simple.clinic.patient.medicalRecords

import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.onlinelookup.api.CompleteMedicalRecordPayload
import org.simple.clinic.patient.onlinelookup.api.RecordRetention
import org.simple.clinic.patient.onlinelookup.api.RetentionType
import org.simple.clinic.patient.onlinelookup.api.SecondsDuration
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.util.UtcClock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class PushMedicalRecordsOnline @Inject constructor(
    private val patientSyncApi: PatientSyncApi,
    private val clock: UtcClock,
) {
  fun pushAllMedicalRecordsOnServer(
      medicalRecords: List<CompleteMedicalRecord>
  ): Result {

    if (medicalRecords.isEmpty()) {
      return Result.NothingToPush
    }

    val request = CompleteMedicalRecordsPushRequest(
        patients = medicalRecords.map { mapToPayload(it) }
    )

    return try {
      val response = patientSyncApi
          .pushAllPatientsData(request)
          .execute()

      return when (response.code()) {
        200 -> Result.Success
        else -> Result.ServerError(
            code = response.code(),
            message = response.errorBody()?.string()
        )
      }

    } catch (e: Exception) {
      Result.NetworkError(e)
    }
  }


  fun mapToPayload(
      completeMedicalRecord: CompleteMedicalRecord
  ): CompleteMedicalRecordPayload {

    val patientProfile = completeMedicalRecord.patient
    val patient = patientProfile.patient

    return CompleteMedicalRecordPayload(
        id = patient.uuid,
        fullName = patient.fullName,
        gender = patient.gender,
        dateOfBirth = patient.ageDetails.dateOfBirth,
        age = patient.ageDetails.ageValue,
        ageUpdatedAt = patient.ageDetails.ageUpdatedAt,
        status = patient.status,
        createdAt = patient.createdAt,
        updatedAt = patient.updatedAt,
        deletedAt = patient.deletedAt,

        address = patientProfile.address.toPayload(),

        phoneNumbers = patientProfile.phoneNumbers
            .map { it.toPayload() },

        businessIds = patientProfile.businessIds
            .map { it.toPayload() },

        recordedAt = patient.recordedAt,

        reminderConsent = patient.reminderConsent,

        deletedReason = patient.deletedReason,

        registeredFacilityId = patient.registeredFacilityId,

        assignedFacilityId = patient.assignedFacilityId,

        appointments = completeMedicalRecord.appointments
            .map { it.toPayload() },

        bloodPressures = completeMedicalRecord.bloodPressures
            .map { it.toPayload() },

        bloodSugars = completeMedicalRecord.bloodSugars
            .map { it.toPayload() },

        medicalHistory = completeMedicalRecord.medicalHistory
            ?.toPayload(),

        prescribedDrugs = completeMedicalRecord.prescribedDrugs
            .map { it.toPayload() },

        retention = patient.retainUntil?.let { retainUntil ->
          val duration = Duration.between(
              Instant.now(clock),
              retainUntil
          ).coerceAtLeast(Duration.ZERO)

          RecordRetention(
              type = RetentionType.Temporary,
              retainFor = SecondsDuration(duration)
          )
        } ?: RecordRetention(
            type = RetentionType.Permanent,
            retainFor = null
        )
    )
  }

  sealed class Result {
    data object Success : Result()
    data object NothingToPush : Result()
    data class ServerError(
        val code: Int,
        val message: String?
    ) : Result()

    data class NetworkError(val throwable: Throwable) : Result()
  }
}
