package org.simple.clinic.teleconsultlog.teleconsultrecord

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.util.UUID

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
) {

  fun toPayload(): TeleconsultRecordPayload {

    val teleconsultRequestInfoPayload = if (teleconsultRequestInfo != null) {
      TeleconsultRequestInfoPayload(
          requesterId = teleconsultRequestInfo.requesterId,
          facilityId = teleconsultRequestInfo.facilityId,
          requestedAt = teleconsultRequestInfo.requestedAt
      )
    } else {
      null
    }
// keeping this as an option as we may or may not add list of prescription UUID's to the TeleconsultRecordInfoPayload
//    val teleconsultRecordInfoPayload = if (teleconsultRecordInfo != null) {
//      TeleconsultRecordInfoPayload(
//          recordedAt = teleconsultRecordInfo.recordedAt,
//          teleconsultationType = teleconsultRecordInfo.teleconsultationType,
//          patientTookMedicines = teleconsultRecordInfo.patientTookMedicines,
//          patientConsented = teleconsultRecordInfo.patientConsented,
//          medicalOfficerNumber = teleconsultRecordInfo.medicalOfficerNumber.toString(),
//      )
//    } else {
//      null
//    }
    return TeleconsultRecordPayload(
        id = id,
        patientId = patientId,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfoPayload,
        teleconsultRecordInfo = null,
        createdAt = timestamp.createdAt,
        updatedAt = timestamp.updatedAt,
        deletedAt = timestamp.deletedAt
    )
  }

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(teleconsultRecords: List<TeleconsultRecord>)

    @Query("DELETE FROM TeleconsultRecord")
    fun clear()

    @Query("SELECT * FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun recordsWithSyncStatus(syncStatus: SyncStatus): List<TeleconsultRecord>

    @Query("SELECT COUNT(id) FROM TeleconsultRecord")
    fun count(): Flowable<Int>

    @Query("UPDATE TeleconsultRecord SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStates(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE TeleconsultRecord SET syncStatus = :newStatus WHERE id in (:uuids) ")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT COUNT(id) FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("SELECT id FROM TeleconsultRecord WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("SELECT * FROM TeleconsultRecord")
    fun getAll() : List<TeleconsultRecord>

  }
}
