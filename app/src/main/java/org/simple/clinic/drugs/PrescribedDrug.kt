package org.simple.clinic.drugs

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID

/**
 * Drugs prescribed to a patient. This may have been picked
 * from a [ProtocolDrug] or entered manually.
 */
@Parcelize
@Entity(indices = [Index("patientUuid", unique = false)])
data class PrescribedDrug(
    @PrimaryKey
    val uuid: UUID,

    val name: String,

    val dosage: String?,

    val rxNormCode: String?,

    val isDeleted: Boolean,

    val isProtocolDrug: Boolean,

    val patientUuid: UUID,

    val facilityUuid: UUID,

    val syncStatus: SyncStatus,

    @Embedded
    val timestamps: Timestamps,

    val frequency: MedicineFrequency?,

    val durationInDays: Int?,

    val teleconsultationId: UUID?
) : Parcelable {

  val createdAt: Instant
    get() = timestamps.createdAt

  val updatedAt: Instant
    get() = timestamps.updatedAt

  val deletedAt: Instant?
    get() = timestamps.deletedAt

  fun toPayload(): PrescribedDrugPayload {
    return PrescribedDrugPayload(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = rxNormCode,
        isDeleted = isDeleted,
        isProtocolDrug = isProtocolDrug,
        patientId = patientUuid,
        facilityId = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        frequency = frequency,
        durationInDays = durationInDays,
        teleconsultationId = teleconsultationId
    )
  }

  fun refill(
      uuid: UUID,
      facilityUuid: UUID,
      utcClock: UtcClock
  ): PrescribedDrug {
    return copy(
        uuid = uuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.PENDING,
        timestamps = Timestamps.create(utcClock),
        frequency = null,
        durationInDays = null,
        teleconsultationId = null
    )
  }

  fun refillForTeleconsultation(
      uuid: UUID,
      facilityUuid: UUID,
      teleconsultationId: UUID,
      utcClock: UtcClock
  ): PrescribedDrug {
    return copy(
        uuid = uuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.PENDING,
        timestamps = Timestamps.create(utcClock),
        frequency = null,
        durationInDays = null,
        teleconsultationId = teleconsultationId
    )
  }

  @Dao
  abstract class RoomDao {

    @Query("SELECT * FROM prescribeddrug WHERE syncStatus = :status")
    abstract fun withSyncStatus(status: SyncStatus): List<PrescribedDrug>

    @Query("""
      SELECT * FROM prescribeddrug
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    abstract fun withSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<PrescribedDrug>

    @Query("UPDATE prescribeddrug SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    abstract fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE prescribeddrug SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    abstract fun updateSyncStatusForIds(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(newDrugs: List<PrescribedDrug>)

    @Transaction
    open fun softDeletePrescription(
        prescriptionUuid: UUID,
        timestamp: Instant
    ) {
      softDelete(prescriptionUuid, deleted = true, updatedAt = timestamp)
      updateSyncStatusForIds(listOf(prescriptionUuid), SyncStatus.PENDING)
    }

    /**
     * [deleted] exists only to trigger Room's Boolean type converter.
     * */
    @Query("UPDATE prescribeddrug SET isDeleted = :deleted, updatedAt = :updatedAt WHERE uuid = :prescriptionId")
    abstract fun softDelete(prescriptionId: UUID, deleted: Boolean, updatedAt: Instant)

    @Query("SELECT * FROM prescribeddrug WHERE uuid = :uuid LIMIT 1")
    abstract fun getOne(uuid: UUID): PrescribedDrug?

    @Query("SELECT uuid FROM prescribeddrug WHERE syncStatus = :syncStatus")
    abstract fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("SELECT COUNT(*) FROM prescribeddrug")
    abstract fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM PrescribedDrug WHERE syncStatus = :syncStatus")
    abstract fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("SELECT * FROM prescribeddrug WHERE patientUuid = :patientUuid AND isDeleted = 0 ORDER BY updatedAt DESC")
    abstract fun forPatient(patientUuid: UUID): Flowable<List<PrescribedDrug>>

    @Query("SELECT * FROM prescribeddrug WHERE patientUuid = :patientUuid AND isDeleted = 0 ORDER BY updatedAt DESC")
    abstract fun forPatientImmediate(patientUuid: UUID): List<PrescribedDrug>

    @Query("DELETE FROM prescribeddrug")
    abstract fun clearData(): Int

    @Query("SELECT * FROM PrescribedDrug WHERE uuid = :prescriptionUuid")
    abstract fun prescription(prescriptionUuid: UUID): Flowable<PrescribedDrug>

    @Query("SELECT * FROM PrescribedDrug WHERE uuid = :prescriptionUuid")
    abstract fun prescriptionImmediate(prescriptionUuid: UUID): PrescribedDrug?

    @Query("""
        SELECT (
            CASE
                WHEN (COUNT(uuid) > 0) THEN 1
                ELSE 0
            END
        )
        FROM PrescribedDrug
        WHERE updatedAt > :instantToCompare AND syncStatus = :pendingStatus AND patientUuid = :patientUuid
    """)
    abstract fun hasPrescriptionForPatientChangedSince(
        patientUuid: UUID,
        instantToCompare: Instant,
        pendingStatus: SyncStatus
    ): Boolean

    @Query("""
      DELETE FROM PrescribedDrug
      WHERE isDeleted = 1 AND syncStatus == 'DONE'
    """)
    abstract fun purgeDeleted()

    @Query("UPDATE PrescribedDrug SET durationInDays = :durationInDays, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE uuid = :id")
    abstract fun updateDrugDuration(
        id: UUID,
        durationInDays: Int,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    @Query("UPDATE PrescribedDrug SET frequency = :drugFrequency, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE uuid = :id")
    abstract fun updateDrugFrequenecy(
        id: UUID,
        drugFrequency: MedicineFrequency,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    @Query("UPDATE PrescribedDrug SET teleconsultationId = :teleconsultationId, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE uuid IN (:drugUuids)")
    abstract fun addTeleconsultationIdToDrugs(
        drugUuids: List<UUID>,
        teleconsultationId: UUID,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    /**
     * [deleted] exists only to trigger Room's Boolean type converter.
     * */
    @Query("UPDATE PrescribedDrug SET isDeleted = :deleted, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE uuid IN (:prescriptionIds)")
    abstract fun softDeleteIds(
        prescriptionIds: List<UUID>,
        deleted: Boolean,
        updatedAt: Instant,
        syncStatus: SyncStatus
    )

    @Query(""" SELECT * FROM PrescribedDrug """)
    abstract fun getAllPrescribedDrugs(): List<PrescribedDrug>

    @Query("""
        DELETE FROM PrescribedDrug
        WHERE 
            uuid NOT IN (
                SELECT PD.uuid FROM PrescribedDrug PD
                INNER JOIN Patient P ON P.uuid == PD.patientUuid
            ) AND
            syncStatus == 'DONE'
    """)
    abstract fun deleteWithoutLinkedPatient()

    @Query("""
        DELETE FROM PrescribedDrug
        WHERE patientUuid IN (
		        SELECT PD.patientUuid 
			      FROM PrescribedDrug PD
			      LEFT JOIN Patient P ON P.uuid == PD.patientUuid
			      WHERE P.uuid IS NULL AND PD.syncStatus == 'DONE'
		    )
    """)
    abstract fun purgePrescribedDrugWhenPatientIsNull()
  }
}
