package org.simple.clinic.encounter

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.encounter.sync.EncounterPayload
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class EncounterRepository @Inject constructor(
    private val database: AppDatabase,
    private val userClock: UserClock,
    private val utcClock: UtcClock
) : SynceableRepository<ObservationsForEncounter, EncounterPayload> {

  override fun save(records: List<ObservationsForEncounter>): Completable {
    return saveObservationsForEncounters(records)
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<ObservationsForEncounter>> {
    return database.encountersDao().recordsWithSyncStatus(syncStatus).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return database.encountersDao().updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    return database.encountersDao().updateSyncStatus(ids, to)
  }

  override fun mergeWithLocalData(payloads: List<EncounterPayload>): Completable {
    val payloadObservable = Observable.fromIterable(payloads)
    val encountersCanBeOverridden = payloadObservable
        .flatMap { canEncountersBeOverridden(it) }

    return payloadObservable.zipWith(encountersCanBeOverridden)
        .filter { (_, canBeOverridden) -> canBeOverridden }
        .map { (payload, _) -> payload }
        .map(::payloadToEncounters)
        .toList()
        .flatMapCompletable(::saveObservationsForEncounters)
  }

  private fun canEncountersBeOverridden(payload: EncounterPayload): Observable<Boolean> {
    return Observable.fromCallable {
      database.encountersDao()
          .getOne(payload.uuid)
          ?.syncStatus.canBeOverriddenByServerCopy()
    }
  }

  private fun saveObservationsForEncounters(records: List<ObservationsForEncounter>): Completable {
    return Completable.fromAction {
      val bloodPressures = records.flatMap { it.bloodPressures }
      val encounters = records.map { it.encounter }

      with(database) {
        runInTransaction {
          encountersDao().save(encounters)
          bloodPressureDao().save(bloodPressures)
        }
      }
    }
  }

  private fun payloadToEncounters(payload: EncounterPayload): ObservationsForEncounter {
    val bloodPressures = payload.observations.bloodPressureMeasurements.map { bps ->
      bps.toDatabaseModel(syncStatus = DONE, encounterUuid = payload.uuid)
    }
    return ObservationsForEncounter(encounter = payload.toDatabaseModel(DONE), bloodPressures = bloodPressures)
  }

  override fun recordCount(): Observable<Int> {
    return database.encountersDao().recordCount()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return database.encountersDao().recordCount(syncStatus = PENDING)
  }

  fun saveBloodPressureMeasurement(
      bloodPressureMeasurement: BloodPressureMeasurement
  ): Completable {
    val encounter = with(bloodPressureMeasurement) {
      Encounter(
          uuid = encounterUuid,
          patientUuid = patientUuid,
          encounteredOn = recordedAt.toLocalDateAtZone(userClock.zone),
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,
          syncStatus = PENDING
      )
    }

    return saveObservationsForEncounters(listOf(ObservationsForEncounter(
        encounter = encounter,
        bloodPressures = listOf(bloodPressureMeasurement)
    )))
  }

  fun updateBloodPressure(measurement: BloodPressureMeasurement): Completable {
    val oldEncounterUuid = measurement.encounterUuid
    val newEncounterUuid = with(measurement) {
      generateEncounterUuid(facilityUuid, patientUuid, recordedAt.toLocalDateAtZone(userClock.zone))
    }

    val updatedMeasurement = measurement.copy(
        encounterUuid = newEncounterUuid,
        updatedAt = Instant.now(utcClock),
        syncStatus = PENDING
    )
    val bpSave = Completable.fromAction {
      database.bloodPressureDao().save(listOf(updatedMeasurement))
    }

    return when (oldEncounterUuid == newEncounterUuid) {
      true -> database.encountersDao()
          .encounter(oldEncounterUuid)
          .take(1)
          .flatMapCompletable { updateEncounter(it).andThen(bpSave) }
      false -> saveBloodPressureMeasurement(updatedMeasurement).andThen(deleteEncounter(oldEncounterUuid))
    }
  }

  private fun deleteEncounter(encounterUuid: UUID): Completable {
    return Completable.fromAction {
      val now = Instant.now(utcClock)
      database.encountersDao().deleteEncounter(
          encounterUuid = encounterUuid,
          deletedAt = now,
          updatedAt = now,
          syncStatus = PENDING
      )
    }
  }

  private fun updateEncounter(encounter: Encounter): Completable {
    return Completable.fromAction {
      val updatedEncounter = encounter.copy(
          syncStatus = PENDING,
          updatedAt = Instant.now(utcClock))
      database.encountersDao().save(updatedEncounter)
    }
  }
}
