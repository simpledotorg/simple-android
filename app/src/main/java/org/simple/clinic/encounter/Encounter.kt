package org.simple.clinic.encounter

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity
data class Encounter(

    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val encounteredOn: LocalDate,

    val syncStatus: SyncStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(encounters: List<Encounter>)

    @Insert(onConflict = REPLACE)
    fun save(encounter: Encounter)

    @Query("UPDATE Encounter SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable

    @Query("UPDATE Encounter SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus): Completable

    @Query("SELECT * FROM Encounter WHERE syncStatus = :syncStatus")
    fun recordsWithSyncStatus(syncStatus: SyncStatus): Observable<List<Encounter>>

    @Query("SELECT COUNT(uuid) FROM Encounter")
    fun recordCount(): Observable<Int>

    @Query("SELECT COUNT(uuid) FROM Encounter WHERE syncStatus = :syncStatus")
    fun recordCount(syncStatus: SyncStatus): Observable<Int>

    @Query("SELECT * FROM Encounter WHERE uuid = :uuid")
    fun getOne(uuid: UUID): Encounter?
  }
}
