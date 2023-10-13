package org.simple.clinic.teleconsultlog.teleconsultrecord

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@Parcelize
@Entity
data class TeleconsultRecord(
    @PrimaryKey
    val id: UUID,

    val patientId: UUID,

    val medicalOfficerId: UUID,

    @Embedded(prefix = "request_")
    val teleconsultRequestInfo: TeleconsultRequestInfo?,

    @Embedded(prefix = "record_")
    val teleconsultRecordInfo: TeleconsultRecordInfo?,

    @Embedded
    val timestamp: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(teleconsultRecords: List<TeleconsultRecord>)

    @Query("DELETE FROM TeleconsultRecord")
    fun clear()

    @Query("SELECT * FROM TeleconsultRecord")
    fun getAll(): List<TeleconsultRecord>

    @Query("SELECT * FROM TeleconsultRecord WHERE id = :teleconsultRecordId")
    fun getCompleteTeleconsultLog(teleconsultRecordId: UUID): TeleconsultRecord

    @Query("UPDATE TeleconsultRecord SET record_medicalOfficerNumber = :medicalOfficerNumber, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :teleconsultRecordId")
    fun updateMedicalRegistrationId(
        teleconsultRecordId: UUID,
        medicalOfficerNumber: String,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    @Query("SELECT * FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun recordsWithSyncStatus(syncStatus: SyncStatus): List<TeleconsultRecord>

    @Query("""
      SELECT * FROM TeleconsultRecord
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<TeleconsultRecord>

    @Query("SELECT COUNT(id) FROM TeleconsultRecord")
    fun count(): Flowable<Int>

    @Query("UPDATE TeleconsultRecord SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStates(oldStatus: SyncStatus, newStatus: SyncStatus)

    @RawQuery
    fun updateSyncStatusRaw(query: SimpleSQLiteQuery): Int

    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus) {
      updateSyncStatusRaw(SimpleSQLiteQuery(
          "UPDATE TeleconsultRecord SET syncStatus = '$newStatus' WHERE id IN (${uuids.joinToString(prefix = "'", postfix = "'", separator = "','")})"
      ))
    }

    @Query("SELECT COUNT(id) FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("SELECT id FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("UPDATE TeleconsultRecord SET request_requesterCompletionStatus = :teleconsultStatus, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :teleconsultRecordId")
    fun updateRequesterCompletionStatus(
        teleconsultRecordId: UUID,
        teleconsultStatus: TeleconsultStatus,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    @Query("""
      SELECT * FROM TeleconsultRecord
      WHERE patientId = :patientUuid AND deletedAt IS NULL
      ORDER BY createdAt DESC LIMIT 1
    """)
    fun latestTeleconsultRecord(patientUuid: UUID): TeleconsultRecord?
  }
}
