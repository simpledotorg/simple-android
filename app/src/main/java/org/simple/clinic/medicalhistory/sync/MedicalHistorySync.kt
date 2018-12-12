package org.simple.clinic.medicalhistory.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class MedicalHistorySync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: MedicalHistoryRepository,
    private val api: MedicalHistorySyncApiV2,
    @Named("last_medicalhistory_pull_token") private val lastPullToken: Preference<Optional<String>>
): ModelSync {

  override fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  override fun push(): Completable {
    return syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  override fun pull(): Completable {
    return syncCoordinator.pull(
        repository = repository,
        lastPullToken = lastPullToken,
        pullNetworkCall = api::pull)
  }

  private fun toRequest(histories: List<MedicalHistory>): MedicalHistoryPushRequest {
    val payloads = histories
        .map {
          it.run {
            MedicalHistoryPayload(
                uuid = uuid,
                patientUuid = patientUuid,
                diagnosedWithHypertension = diagnosedWithHypertension,
                isOnTreatmentForHypertension = isOnTreatmentForHypertension,
                hasHadHeartAttack = hasHadHeartAttack,
                hasHadStroke = hasHadStroke,
                hasHadKidneyDisease = hasHadKidneyDisease,
                hasDiabetes = hasDiabetes,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt)
          }
        }
    return MedicalHistoryPushRequest(payloads)
  }
}
