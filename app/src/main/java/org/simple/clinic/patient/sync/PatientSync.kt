package org.simple.clinic.patient.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class PatientSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: PatientRepository,
    private val api: PatientSyncApiV1,
    @Named("last_patient_pull_token") private val lastPullToken: Preference<Optional<String>>
) : ModelSync {

  override fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  override fun push() = syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)) })

  override fun pull() = syncCoordinator.pull(repository, lastPullToken, api::pull)

  private fun toRequest(patients: List<PatientProfile>): PatientPushRequest {
    return PatientPushRequest(
        patients.map { (patient, address, phoneNumbers) ->
          val numberPayloads = phoneNumbers
              .map(::phoneNumberPayload)
              .let { payloads -> if (payloads.isEmpty()) null else payloads }

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
                phoneNumbers = numberPayloads,
                address = address.toPayload()
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
          updatedAt = updatedAt
      )
    }
  }
}
