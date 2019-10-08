package org.simple.clinic.bp

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity(indices = [Index("patientUuid", unique = false)])
data class BloodPressureMeasurement(
    @PrimaryKey
    val uuid: UUID,

    val systolic: Int,

    val diastolic: Int,

    val syncStatus: SyncStatus,

    val userUuid: UUID,

    val facilityUuid: UUID,

    val patientUuid: UUID,

    val encounterUuid: UUID,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?,

    val recordedAt: Instant
) {

  @Transient
  val level = BloodPressureLevel.compute(this)

  fun toPayload(): BloodPressureMeasurementPayload {
    return BloodPressureMeasurementPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        systolic = systolic,
        diastolic = diastolic,
        facilityUuid = facilityUuid,
        userUuid = userUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM bloodpressuremeasurement WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<BloodPressureMeasurement>>

    @Query("UPDATE bloodpressuremeasurement SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE bloodpressuremeasurement SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newMeasurements: List<BloodPressureMeasurement>)

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): BloodPressureMeasurement?

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid")
    fun bloodPressure(uuid: UUID): Flowable<BloodPressureMeasurement>

    @Query("SELECT COUNT(uuid) FROM bloodpressuremeasurement")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM BloodPressureMeasurement WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("""
      SELECT COUNT(uuid)
      FROM bloodpressuremeasurement
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
    """)
    fun recordedBloodPressureCountForPatient(patientUuid: UUID): Flowable<Int>

    @Query("""
      SELECT * FROM bloodpressuremeasurement
        WHERE patientUuid = :patientUuid AND deletedAt IS NULL
        ORDER BY recordedAt DESC LIMIT :limit
    """)
    fun newestMeasurementsForPatient(patientUuid: UUID, limit: Int): Flowable<List<BloodPressureMeasurement>>

    @Query("DELETE FROM bloodpressuremeasurement")
    fun clearData(): Int

    @Query("""
      SELECT patientUuid, facilityUuid
      FROM bloodpressuremeasurement
      WHERE patientUuid IN (:patientUuids) AND deletedAt IS NULL
      """)
    fun patientToFacilityIds(patientUuids: List<UUID>): Flowable<List<PatientToFacilityId>>

    @Query("""
        SELECT (
            CASE
                WHEN (COUNT(uuid) > 0) THEN 1
                ELSE 0
            END
        )
        FROM BloodPressureMeasurement
        WHERE updatedAt > :instantToCompare AND syncStatus = :pendingStatus AND patientUuid = :patientUuid
    """)
    fun haveBpsForPatientChangedSince(
        patientUuid: UUID,
        instantToCompare: Instant,
        pendingStatus: SyncStatus
    ): Flowable<Boolean>
  }
}
