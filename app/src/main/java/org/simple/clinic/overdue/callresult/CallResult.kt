package org.simple.clinic.overdue.callresult

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity(tableName = "CallResult")
data class CallResult(

    @PrimaryKey
    val id: UUID,

    val userId: UUID,

    val appointmentId: UUID,

    val removeReason: AppointmentCancelReason?,

    val outcome: Outcome,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(callResults: List<CallResult>)

    @Query("""
        UPDATE CallResult
        SET syncStatus = :newStatus
        WHERE syncStatus = :oldStatus
    """)
    fun updateSyncStatus(
        oldStatus: SyncStatus,
        newStatus: SyncStatus
    )

    @Query("""
      UPDATE CallResult
      SET syncStatus = :newStatus
      WHERE id IN (:callResultIds)
    """)
    fun updateSyncStatusForIds(
        callResultIds: List<UUID>,
        newStatus: SyncStatus
    )

    @Query("""
      SELECT COUNT(id)
      FROM CallResult
    """)
    fun recordCount(): Observable<Int>

    @Query("""
      SELECT COUNT(id)
      FROM CallResult
      WHERE syncStatus = :syncStatus
    """)
    fun countWithStatus(syncStatus: SyncStatus): Observable<Int>
  }
}
