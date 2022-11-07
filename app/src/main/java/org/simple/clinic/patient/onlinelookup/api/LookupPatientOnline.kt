package org.simple.clinic.patient.onlinelookup.api

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.Found
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.NotFound
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.OtherError
import org.simple.clinic.patient.sync.ForPatientSync
import org.simple.clinic.patient.sync.ForPatientSync.Type.RecordRetentionFallbackDuration
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.UtcClock
import retrofit2.Response
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class LookupPatientOnline @Inject constructor(
    private val patientSyncApi: PatientSyncApi,
    private val clock: UtcClock,
    @ForPatientSync(RecordRetentionFallbackDuration) private val fallbackRecordRetentionDuration: Duration
) {

  fun lookupWithIdentifier(identifier: String): Result {
    return try {
      lookupPatientOnServer(identifier)
    } catch (e: Exception) {
      CrashReporter.report(Throwable("Failed to lookup patient:", e))
      OtherError
    }
  }

  private fun lookupPatientOnServer(identifier: String): Result {
    val response = patientSyncApi.lookup(PatientOnlineLookupRequest(identifier)).execute()

    return when (response.code()) {
      200 -> readSuccessResponse(response, identifier)
      404 -> NotFound(identifier)
      else -> OtherError
    }
  }

  private fun readSuccessResponse(
      response: Response<OnlineLookupResponsePayload>,
      identifier: String
  ): Result {
    val responseBody = response.body()!!
    val medicalRecords = responseBody.patients.map(::convertResponseToMedicalRecord)

    return if (medicalRecords.isNotEmpty()) Found(medicalRecords) else NotFound(identifier)
  }

  private fun convertResponseToMedicalRecord(
      response: CompleteMedicalRecordPayload
  ) = CompleteMedicalRecord(
      patient = readPatientProfileFromResponse(response),
      medicalHistory = readMedicalHistoryFromResponse(response),
      appointments = readAppointmentsFromResponse(response),
      bloodSugars = readBloodSugarsFromResponse(response),
      bloodPressures = readBloodPressuresFromResponse(response),
      prescribedDrugs = readPrescribedDrugsFromResponse(response)
  )

  private fun readPatientProfileFromResponse(
      response: CompleteMedicalRecordPayload,
  ): PatientProfile {
    val ageDetails = PatientAgeDetails(
        ageValue = response.age,
        ageUpdatedAt = response.ageUpdatedAt,
        dateOfBirth = response.dateOfBirth
    )

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

    return PatientProfile(
        patient = Patient(
            uuid = response.id,
            addressUuid = response.address.uuid,
            fullName = response.fullName,
            gender = response.gender,
            ageDetails = ageDetails,
            status = response.status,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
            deletedAt = response.deletedAt,
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
  }

  private fun readMedicalHistoryFromResponse(
      response: CompleteMedicalRecordPayload,
  ): MedicalHistory? {
    return if (response.medicalHistory != null) {
      MedicalHistory(
          uuid = response.medicalHistory.uuid,
          patientUuid = response.medicalHistory.patientUuid,
          diagnosedWithHypertension = response.medicalHistory.hasHypertension ?: Answer.Unanswered,
          isOnHypertensionTreatment = response.medicalHistory.isOnTreatmentForHypertension,
          isOnDiabetesTreatment = response.medicalHistory.isOnDiabetesTreatment ?: Answer.Unanswered,
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
  }

  private fun readAppointmentsFromResponse(response: CompleteMedicalRecordPayload): List<Appointment> {
    return response.appointments.map { payload ->
      Appointment(
          uuid = payload.uuid,
          patientUuid = payload.patientUuid,
          facilityUuid = payload.facilityUuid,
          scheduledDate = payload.date,
          status = payload.status,
          cancelReason = payload.cancelReason,
          remindOn = payload.remindOn,
          agreedToVisit = payload.agreedToVisit,
          appointmentType = payload.appointmentType,
          syncStatus = SyncStatus.DONE,
          createdAt = payload.createdAt,
          updatedAt = payload.updatedAt,
          deletedAt = payload.deletedAt,
          creationFacilityUuid = payload.creationFacilityUuid)
    }
  }

  private fun readBloodPressuresFromResponse(response: CompleteMedicalRecordPayload): List<BloodPressureMeasurement> {
    return response.bloodPressures.map { payload ->
      payload.toDatabaseModel(SyncStatus.DONE)
    }
  }

  private fun readBloodSugarsFromResponse(response: CompleteMedicalRecordPayload): List<BloodSugarMeasurement> {
    return response.bloodSugars.map { payload ->
      payload.toDatabaseModel(SyncStatus.DONE)
    }
  }

  private fun readPrescribedDrugsFromResponse(response: CompleteMedicalRecordPayload): List<PrescribedDrug> {
    return response.prescribedDrugs.map { payload ->
      payload.toDatabaseModel(SyncStatus.DONE)
    }
  }

  sealed class Result {
    data class Found(val medicalRecords: List<CompleteMedicalRecord>) : Result()
    data class NotFound(val identifier: String) : Result()
    object OtherError : Result()
  }
}
