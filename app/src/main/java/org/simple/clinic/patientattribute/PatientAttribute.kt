package org.simple.clinic.patientattribute

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
    tableName = "PatientAttribute",
    indices = [
      Index("patientUuid", unique = false)
    ]
)
data class PatientAttribute(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val userUuid: UUID,

    @Embedded
    val reading: BMIReading,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable {

  @Dao
  interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAttribute(patientAttribute: PatientAttribute)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAttributes(patientAttributes: List<PatientAttribute>)

    @Query("UPDATE PatientAttribute SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @RawQuery
    fun updateSyncStatusForIdsRaw(query: SimpleSQLiteQuery): Int

    fun updateSyncStatusForIds(ids: List<UUID>, to: SyncStatus) {
      updateSyncStatusForIdsRaw(SimpleSQLiteQuery(
          "UPDATE PatientAttribute SET syncStatus = '$to' WHERE uuid IN (${ids.joinToString(prefix = "'", postfix = "'", separator = "','")})"
      ))
    }

    @Query("SELECT COUNT(uuid) FROM PatientAttribute")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM PatientAttribute WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("""
      SELECT * FROM PatientAttribute
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<PatientAttribute>

    @Query("SELECT uuid FROM PatientAttribute WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("""
      SELECT * FROM PatientAttribute
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
      ORDER BY updatedAt DESC
      LIMIT 1
    """)
    fun patientAttributeImmediate(patientUuid: UUID): PatientAttribute?

    @Query("DELETE FROM PatientAttribute")
    fun clear()
  }
}
