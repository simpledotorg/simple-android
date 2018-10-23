package org.simple.clinic.medicalhistory.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class MedicalHistorySync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: MedicalHistoryRepository,
    private val api: MedicalHistorySyncApiV1,
    @Named("last_medicalhistory_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    return syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  fun pull(): Completable {
    return syncCoordinator.pull(
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
                diagnosedWithHypertension = Answer.toBoolean(diagnosedWithHypertension),
                hasHadHeartAttack = Answer.toBoolean(hasHadHeartAttack),
                hasHadStroke = Answer.toBoolean(hasHadStroke),
                hasHadKidneyDisease = Answer.toBoolean(hasHadKidneyDisease),
                isOnTreatmentForHypertension = Answer.toBoolean(isOnTreatmentForHypertension),
                hasDiabetes = Answer.toBoolean(hasDiabetes),
                createdAt = createdAt,
                updatedAt = updatedAt)
          }
        }
    return MedicalHistoryPushRequest(payloads)
  }
}
