package org.resolvetosavelives.red.bp

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.resolvetosavelives.red.bp.sync.BloodPressureMeasurementPayload
import org.resolvetosavelives.red.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity
data class BloodPressureMeasurement(
    @PrimaryKey
    val uuid: UUID,

    val systolic: Int,

    val diastolic: Int,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    val patientUuid: UUID
) {

  @Transient
  val riskLevel = BloodPressureRiskLevel.compute(this)

  fun toPayload(): BloodPressureMeasurementPayload {
    return BloodPressureMeasurementPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        systolic = systolic,
        diastolic = diastolic,
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newMeasurement: BloodPressureMeasurement)

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid LIMIT 1")
    fun get(uuid: UUID): BloodPressureMeasurement?

    @Query("SELECT COUNT(*) FROM bloodpressuremeasurement")
    fun measurementCount(): Flowable<Int>

    @Query("SELECT * FROM bloodpressuremeasurement WHERE patientUuid = :patientUuid ORDER BY updatedAt DESC LIMIT 100")
    fun measurementForPatient(patientUuid: UUID): Flowable<List<BloodPressureMeasurement>>
  }
}
