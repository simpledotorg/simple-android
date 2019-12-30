package org.simple.clinic.bloodsugar

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "BloodSugarMeasurements")
data class BloodSugarMeasurement(
    @PrimaryKey
    val uuid: UUID,

    @Embedded(prefix = "reading_")
    val reading: BloodSugarReading,

    val recordedAt: Instant,

    val patientUuid: UUID,

    val userUuid: UUID,

    val facilityUuid: UUID,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(bloodSugars: List<BloodSugarMeasurement>)

    @Query("""
      SELECT * FROM BloodSugarMeasurements
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
      ORDER BY recordedAt DESC LIMIT :limit
    """)
    fun latestMeasurements(patientUuid: UUID, limit: Int): Observable<List<BloodSugarMeasurement>>
  }
}
