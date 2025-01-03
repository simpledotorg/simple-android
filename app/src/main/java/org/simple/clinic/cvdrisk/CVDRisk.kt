package org.simple.clinic.cvdrisk

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
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
import java.util.UUID

@Parcelize
@Entity(
    tableName = "CVDRisk",
    indices = [
      Index("patientUuid", unique = false)
    ]
)
data class CVDRisk(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val riskScore: String,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable {

  @Dao
  interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRisk(cvdRisk: CVDRisk)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRisks(cvdRisks: List<CVDRisk>)

    @Query("UPDATE CVDRisk SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @RawQuery
    fun updateSyncStatusForIdsRaw(query: SimpleSQLiteQuery): Int

    fun updateSyncStatusForIds(ids: List<UUID>, to: SyncStatus) {
      updateSyncStatusForIdsRaw(SimpleSQLiteQuery(
          "UPDATE CVDRisk SET syncStatus = '$to' WHERE uuid IN (${ids.joinToString(prefix = "'", postfix = "'", separator = "','")})"
      ))
    }

    @Query("SELECT COUNT(uuid) FROM CVDRISK")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM CVDRisk WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("SELECT * FROM CVDRisk WHERE syncStatus = :status")
    fun recordsWithSyncStatus(status: SyncStatus): List<CVDRisk>

    @Query("""
      SELECT * FROM CVDRisk
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<CVDRisk>

    @Query("SELECT uuid FROM CVDRisk WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("""
      SELECT * FROM CVDRisk
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
      ORDER BY updatedAt DESC
      LIMIT 1
    """)
    fun cvdRiskImmediate(patientUuid: UUID): CVDRisk?

    @Query("DELETE FROM CVDRisk")
    fun clear()
  }
}
