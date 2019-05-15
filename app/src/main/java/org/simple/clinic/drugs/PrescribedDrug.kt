package org.simple.clinic.drugs

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

/**
 * Drugs prescribed to a patient. This may have been picked
 * from a [ProtocolDrug] or entered manually.
 */
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

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

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
        recordedAt = createdAt)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM prescribeddrug WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<PrescribedDrug>>

    @Query("UPDATE prescribeddrug SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE prescribeddrug SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newDrugs: List<PrescribedDrug>)

    /**
     * [deleted] exists only to trigger Room's Boolean type converter.
     * */
    @Query("UPDATE prescribeddrug SET isDeleted = :deleted, updatedAt = :updatedAt WHERE uuid = :prescriptionId")
    fun softDelete(prescriptionId: UUID, deleted: Boolean, updatedAt: Instant)

    @Query("SELECT * FROM prescribeddrug WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): PrescribedDrug?

    @Query("SELECT COUNT(*) FROM prescribeddrug")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM PrescribedDrug WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("SELECT * FROM prescribeddrug WHERE patientUuid = :patientUuid AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun forPatient(patientUuid: UUID): Flowable<List<PrescribedDrug>>

    @Query("DELETE FROM prescribeddrug")
    fun clearData(): Int

    @Query("SELECT * FROM PrescribedDrug WHERE uuid = :prescriptionUuid")
    fun prescription(prescriptionUuid: UUID): Flowable<PrescribedDrug>
  }
}
