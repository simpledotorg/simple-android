package org.simple.clinic.encounter.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.encounter.EncounterRepository
import org.simple.clinic.encounter.ObservationsForEncounter
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class EncounterSync @Inject constructor(
    private val api: EncounterSyncApi,
    private val syncCoordinator: SyncCoordinator,
    private val repository: EncounterRepository,
    private val userSession: UserSession,
    @Named("last_encounter_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val configProvider: Single<SyncConfig>
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override fun sync(): Completable {
    return canSyncData()
        .flatMapCompletable { canSync ->
          if (canSync) {
            Completable.mergeArrayDelayError(push(), pull())

          } else {
            Completable.complete()
          }
        }
  }

  override fun push() = syncCoordinator.push(
      repository = repository,
      pushNetworkCall = { api.push(toRequest(it)) }
  )

  private fun toRequest(observationsForEncounter: List<ObservationsForEncounter>): EncounterPushRequest {
    val encounterPayloads = observationsForEncounter.map { (encounter, bps) ->
      val observationsPayload = EncounterObservationsPayload(bloodPressureMeasurements = bps.map { it.toPayload() })
      EncounterPayload(
          uuid = encounter.uuid,
          patientUuid = encounter.patientUuid,
          encounteredOn = encounter.encounteredOn,
          createdAt = encounter.createdAt,
          updatedAt = encounter.updatedAt,
          deletedAt = encounter.deletedAt,
          observations = observationsPayload
      )
    }

    return EncounterPushRequest(encounters = encounterPayloads)
  }

  override fun pull(): Completable {
    return configProvider
        .map { it.batchSize }
        .flatMapCompletable { batchSize ->
          syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize.numberOfRecords, it) }
        }
  }

  override fun syncConfig(): Single<SyncConfig> = configProvider
}
