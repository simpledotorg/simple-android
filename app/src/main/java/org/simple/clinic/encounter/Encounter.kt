package org.simple.clinic.encounter

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

data class Encounter(

    val uuid: UUID,

    val patientUuid: UUID,

    val encounteredOn: LocalDate,

    val syncStatus: SyncStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

  interface RoomDao {

    fun save(encounters: List<Encounter>)

    fun save(encounter: Encounter)

    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable

    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus): Completable

    fun recordsWithSyncStatus(syncStatus: SyncStatus): Observable<List<Encounter>>

    fun recordCount(): Observable<Int>

    fun recordCount(syncStatus: SyncStatus): Observable<Int>

    fun getOne(uuid: UUID): Encounter?
  }
}
