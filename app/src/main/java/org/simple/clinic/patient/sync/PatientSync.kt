package org.simple.clinic.patient.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

class PatientSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: PatientRepository,
    private val api: PatientSyncApi,
    @Named("last_patient_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Patient"

  override val requiresSyncApprovedUser = true

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  private fun toRequest(patients: List<PatientProfile>): PatientPushRequest {
    return PatientPushRequest(
        patients.map { (patient, address, phoneNumbers, businessIds) ->
          val numberPayloads = phoneNumbers
              .map(::phoneNumberPayload)
              .let { payloads -> if (payloads.isEmpty()) null else payloads }

          val businessIdPayloads = businessIds
              .map { it.toPayload() }

          patient.run {
            PatientPayload(
                uuid = uuid,
                fullName = fullName,
                gender = gender,
                dateOfBirth = ageDetails.dateOfBirth,
                age = ageDetails.ageValue,
                ageUpdatedAt = ageDetails.ageUpdatedAt,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                address = address.toPayload(),
                phoneNumbers = numberPayloads,
                businessIds = businessIdPayloads,
                recordedAt = recordedAt,
                reminderConsent = reminderConsent,
                deletedReason = deletedReason,
                registeredFacilityId = registeredFacilityId,
                assignedFacilityId = assignedFacilityId
            )
          }
        }
    )
  }

  private fun phoneNumberPayload(phoneNumber: PatientPhoneNumber): PatientPhoneNumberPayload {
    return phoneNumber.run {
      PatientPhoneNumberPayload(
          uuid = uuid,
          number = number,
          type = phoneType,
          active = active,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt)
    }
  }
}
