package org.simple.clinic.medicalhistory

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class MedicalHistorySync @Inject constructor(
    private val dataSync: DataSync,
    private val repository: MedicalHistoryRepository,
    private val api: MedicalHistorySyncApiV1,
    private val config: MedicalHistoryConfig,
    @Named("last_medicalhistory_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    if (!config.isSyncEnabled) {
      return Completable.complete()
    }
    return dataSync.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  fun pull(): Completable {
    if (!config.isSyncEnabled) {
      return Completable.complete()
    }

    return dataSync.pull(
        repository = repository,
        lastPullTimestamp = lastPullTimestamp,
        pullNetworkCall = api::pull)
  }

  private fun toRequest(histories: List<MedicalHistory>): MedicalHistoryPushRequest {
    val payloads = histories
        .map {
          it.run {
            MedicalHistoryPayload(
                uuid = uuid,
                patientUuid = patientUuid,
                hasHadHeartAttack = hasHadHeartAttack,
                hasHadStroke = hasHadStroke,
                hasHadKidneyDisease = hasHadKidneyDisease,
                isOnTreatmentForHypertension = isOnTreatmentForHypertension,
                hasDiabetes = hasDiabetes,
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
    return MedicalHistoryPushRequest(payloads)
  }
}
