package org.simple.clinic.summary.teleconsultation.sync

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

@Entity
data class TeleconsultationFacilityInfo(
    @PrimaryKey
    val teleconsultationFacilityId: UUID,

    val facilityId: UUID,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?,

    val syncStatus: SyncStatus
) {

  fun toPayload(medicalOfficers: List<MedicalOfficer>): TeleconsultationFacilityInfoPayload {
    return TeleconsultationFacilityInfoPayload(
        id = teleconsultationFacilityId,
        facilityId = facilityId,
        medicalOfficers = medicalOfficers.map { it.toPayload() },
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(teleconsultationFacilityInfo: List<TeleconsultationFacilityInfo>)

    @Query("DELETE FROM TeleconsultationFacilityInfo")
    fun clear()

    @Query("SELECT COUNT(teleconsultationFacilityId) FROM TeleconsultationFacilityInfo")
    fun count(): Int

    @Query("SELECT COUNT(teleconsultationFacilityId) FROM TeleconsultationFacilityInfo WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Int

    @Query("SELECT * FROM TeleconsultationFacilityInfo WHERE teleconsultationFacilityId = :id")
    fun getOne(id: UUID): TeleconsultationFacilityInfo?

    @Query("SELECT teleconsultationFacilityId FROM TeleconsultationFacilityInfo WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("UPDATE TeleconsultationFacilityInfo SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @RawQuery
    fun updateSyncStatusForIdsRaw(query: SimpleSQLiteQuery): Int

    fun updateSyncStatusForIds(uuids: List<UUID>, newStatus: SyncStatus) {
      updateSyncStatusForIdsRaw(SimpleSQLiteQuery(
          "UPDATE TeleconsultationFacilityInfo SET syncStatus = '$newStatus' WHERE teleconsultationFacilityId IN (${uuids.joinToString(prefix = "'", postfix = "'", separator = "','")})"
      ))
    }

    @Query("SELECT * FROM TeleconsultationFacilityInfo WHERE syncStatus = :syncStatus")
    fun recordsWithSyncStatus(syncStatus: SyncStatus): List<TeleconsultationFacilityInfo>
  }
}
