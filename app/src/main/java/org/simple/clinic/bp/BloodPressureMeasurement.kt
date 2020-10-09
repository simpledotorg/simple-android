package org.simple.clinic.bp

import android.os.Parcelable
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

@Parcelize
@Entity(indices = [Index("patientUuid", unique = false)])
data class BloodPressureMeasurement(
    @PrimaryKey
    val uuid: UUID,

    @Embedded
    val reading: BloodPressureReading,

    val syncStatus: SyncStatus,

    val userUuid: UUID,

    val facilityUuid: UUID,

    val patientUuid: UUID,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?,

    val recordedAt: Instant
) : Parcelable {

  @Transient
  @IgnoredOnParcel
  val level = BloodPressureLevel.compute(this)

  fun toPayload(): BloodPressureMeasurementPayload {
    return BloodPressureMeasurementPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        systolic = reading.systolic,
        diastolic = reading.diastolic,
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
    fun withSyncStatus(status: SyncStatus): List<BloodPressureMeasurement>

    @Query("UPDATE bloodpressuremeasurement SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE bloodpressuremeasurement SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newMeasurements: List<BloodPressureMeasurement>)

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): BloodPressureMeasurement?

    @Query("SELECT uuid FROM bloodpressuremeasurement WHERE syncStatus = :syncStatus")
    fun recordIdsWithStatus(syncStatus: SyncStatus): List<UUID>

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid")
    fun bloodPressure(uuid: UUID): Flowable<BloodPressureMeasurement>

    @Query("SELECT * FROM bloodpressuremeasurement WHERE uuid = :uuid")
    fun bloodPressureImmediate(uuid: UUID): BloodPressureMeasurement

    @Query("SELECT COUNT(uuid) FROM bloodpressuremeasurement")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM BloodPressureMeasurement WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("""
      SELECT COUNT(uuid)
      FROM bloodpressuremeasurement
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
    """)
    fun recordedBloodPressureCountForPatientImmediate(patientUuid: UUID): Int

    @Query("""
      SELECT COUNT(uuid)
      FROM bloodpressuremeasurement
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
    """)
    fun recordedBloodPressureCountForPatient(patientUuid: UUID): Observable<Int>

    @Query("""
      SELECT * FROM bloodpressuremeasurement
        WHERE patientUuid = :patientUuid AND deletedAt IS NULL
        ORDER BY recordedAt DESC LIMIT :limit
    """)
    fun newestMeasurementsForPatient(patientUuid: UUID, limit: Int): Flowable<List<BloodPressureMeasurement>>

    @Query("""
      SELECT * FROM bloodpressuremeasurement
        WHERE patientUuid = :patientUuid AND deletedAt IS NULL
        ORDER BY recordedAt DESC LIMIT :limit
    """)
    fun newestMeasurementsForPatientImmediate(patientUuid: UUID, limit: Int): List<BloodPressureMeasurement>

    @Query("DELETE FROM bloodpressuremeasurement")
    fun clearData(): Int

    @Query("""
      SELECT patientUuid, facilityUuid
      FROM bloodpressuremeasurement
      WHERE patientUuid IN (:patientUuids) AND deletedAt IS NULL
      """)
    fun patientToFacilityIds(patientUuids: List<UUID>): List<PatientToFacilityId>

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
    ): Boolean

    @Query("""
      SELECT * FROM bloodpressuremeasurement
      WHERE patientUuid == :patientUuid AND deletedAt IS NULL
      ORDER BY recordedAt DESC
    """)
    fun allBloodPressures(patientUuid: UUID): Observable<List<BloodPressureMeasurement>>

    @Query("""
      SELECT * FROM bloodpressuremeasurement
      WHERE patientUuid == :patientUuid AND deletedAt IS NULL
      ORDER BY recordedAt DESC
    """)
    fun allBloodPressuresDataSource(patientUuid: UUID): DataSource.Factory<Int, BloodPressureMeasurement>

    @Query("""
      DELETE FROM BloodPressureMeasurement
      WHERE deletedAt IS NOT NULL AND syncStatus == 'DONE'
    """)
    fun purgeDeleted()

    @Query("SELECT * FROM BloodPressureMeasurement")
    fun getAllBloodPressureMeasurements(): List<BloodPressureMeasurement>

    @Query("""
        DELETE FROM BloodPressureMeasurement
        WHERE 
            uuid NOT IN (
                SELECT BP.uuid FROM BloodPressureMeasurement BP
                INNER JOIN Patient P ON P.uuid == BP.patientUuid
            ) AND
            syncStatus == 'DONE'
    """)
    fun deleteWithoutLinkedPatient()
  }
}
