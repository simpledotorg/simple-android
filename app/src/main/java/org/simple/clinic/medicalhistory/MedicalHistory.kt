package org.simple.clinic.medicalhistory

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "MedicalHistory")
data class MedicalHistory(
    @PrimaryKey
    val uuid: UUID,
    val patientUuid: UUID,
    val hasHadHeartAttack: Boolean,
    val hasHadStroke: Boolean,
    val hasHadKidneyDisease: Boolean,
    val isOnTreatmentForHypertension: Boolean,
    val hasDiabetes: Boolean,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM MedicalHistory WHERE syncStatus = :status")
    fun recordsWithSyncStatus(status: SyncStatus): Flowable<List<MedicalHistory>>

    @Query("UPDATE MedicalHistory SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @Query("UPDATE MedicalHistory SET syncStatus = :to WHERE uuid IN (:ids)")
    fun updateSyncStatus(ids: List<UUID>, to: SyncStatus)

    @Query("SELECT * FROM MedicalHistory WHERE uuid = :id LIMIT 1")
    fun getOne(id: UUID): MedicalHistory?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(history: MedicalHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(histories: List<MedicalHistory>)

    @Query("SELECT COUNT(uuid) FROM MedicalHistory")
    fun count(): Flowable<Int>

    @Query("DELETE FROM MedicalHistory")
    fun clear()

    @Query("SELECT * FROM MedicalHistory WHERE patientUuid = :patientUuid")
    fun historyForPatient(patientUuid: PatientUuid): Flowable<List<MedicalHistory>>
  }
}
