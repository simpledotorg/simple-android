package org.simple.clinic.patient.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class PatientSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: PatientRepository,
    private val api: PatientSyncApi,
    @Named("last_patient_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Patient"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable = Completable
      .mergeArrayDelayError(
          Completable.fromAction { push() },
          Completable.fromAction { pull() }
      )

  override fun push() {
    syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)).execute().read()!! })
  }

  override fun pull() {
    val batchSize = config.batchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  override fun syncConfig(): SyncConfig = config

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
                dateOfBirth = dateOfBirth,
                age = age?.value,
                ageUpdatedAt = age?.updatedAt,
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
