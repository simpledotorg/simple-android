package org.simple.clinic.patient.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSaveModel
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class PatientSync2 @Inject constructor(
    private val dataSync: DataSync,
    private val repository: PatientRepository,
    private val api: PatientSyncApiV1,
    @Named("last_patient_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  fun push() = dataSync.push(repository, pushNetworkCall = { api.push(toRequest(it)) })

  fun pull() = dataSync.pull(repository, lastPullTimestamp, api::pull)

  private fun toRequest(patients: List<PatientSaveModel>): PatientPushRequest {
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
