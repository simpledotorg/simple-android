package org.simple.clinic.medicalhistory

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.squareup.moshi.Json
import io.reactivex.Flowable
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "MedicalHistory")
data class MedicalHistory(
    @PrimaryKey
    val uuid: UUID,
    val patientUuid: UUID,
    val diagnosedWithHypertension: Answer,
    val isOnTreatmentForHypertension: Answer,
    val hasHadHeartAttack: Answer,
    val hasHadStroke: Answer,
    val hasHadKidneyDisease: Answer,
    val hasDiabetes: Answer,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
) {

  enum class Answer {
    @Json(name = "yes")
    YES,

    @Json(name = "no")
    NO,

    @Json(name = "unknown")
    UNKNOWN;

    class RoomTypeConverter : RoomEnumTypeConverter<Answer>(Answer::class.java)
  }

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(history: MedicalHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(histories: List<MedicalHistory>)

    @Query("SELECT COUNT(uuid) FROM MedicalHistory")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM MedicalHistory WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("DELETE FROM MedicalHistory")
    fun clear()

    /**
     * The last updated medical history is returned because it's possible
     * to have multiple medical histories present for the same patient.
     * See [MedicalHistoryRepository.historyForPatientOrDefault] to
     * understand when this will happen.
     */
    @Query("""
      SELECT * FROM MedicalHistory
      WHERE patientUuid = :patientUuid
      ORDER BY updatedAt DESC
      LIMIT 1
    """)
    fun historyForPatient(patientUuid: PatientUuid): Flowable<List<MedicalHistory>>
  }
}
