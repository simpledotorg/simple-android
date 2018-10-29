package org.simple.clinic.bp

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity(indices = [Index("patientUuid", unique = false)])
data class BloodPressureMeasurement constructor(
    @PrimaryKey
    val uuid: UUID,

    val systolic: Int,

    val diastolic: Int,

    val syncStatus: SyncStatus,

    val userUuid: UUID,

    val facilityUuid: UUID,

    val patientUuid: UUID,

    val createdAt: Instant,

    val updatedAt: Instant
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
        updatedAt = updatedAt)
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

    @Query("SELECT COUNT(*) FROM bloodpressuremeasurement")
    fun count(): Flowable<Int>

    @Query("SELECT * FROM bloodpressuremeasurement WHERE patientUuid = :patientUuid ORDER BY createdAt DESC LIMIT 100")
    fun newest100ForPatient(patientUuid: UUID): Flowable<List<BloodPressureMeasurement>>

    @Query("DELETE FROM bloodpressuremeasurement")
    fun clearData(): Int

    @Query("SELECT patientUuid, facilityUuid FROM bloodpressuremeasurement WHERE patientUuid IN (:patientUuids)")
    fun patientToFacilityIds(patientUuids: List<UUID>): Flowable<List<PatientToFacilityId>>
  }
}
