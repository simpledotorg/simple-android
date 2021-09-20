package org.simple.clinic.medicalhistory

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_DIABETES_TREATMENT
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_HYPERTENSION_TREATMENT
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

@Parcelize
@Entity(tableName = "MedicalHistory")
data class MedicalHistory(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val diagnosedWithHypertension: Answer,

    val isOnHypertensionTreatment: Answer,

    val isOnDiabetesTreatment: Answer,

    val hasHadHeartAttack: Answer,

    val hasHadStroke: Answer,

    val hasHadKidneyDisease: Answer,

    @ColumnInfo(name = "hasDiabetes")
    val diagnosedWithDiabetes: Answer,

    val syncStatus: SyncStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) : Parcelable {
  val diagnosisRecorded: Boolean
    get() = diagnosedWithHypertension != Unanswered && diagnosedWithDiabetes != Unanswered

  fun answered(question: MedicalHistoryQuestion, answer: Answer): MedicalHistory {
    return when (question) {
      DIAGNOSED_WITH_HYPERTENSION -> copy(diagnosedWithHypertension = answer)
      HAS_HAD_A_HEART_ATTACK -> copy(hasHadHeartAttack = answer)
      HAS_HAD_A_STROKE -> copy(hasHadStroke = answer)
      HAS_HAD_A_KIDNEY_DISEASE -> copy(hasHadKidneyDisease = answer)
      DIAGNOSED_WITH_DIABETES -> copy(diagnosedWithDiabetes = answer)
      IS_ON_HYPERTENSION_TREATMENT -> copy(isOnHypertensionTreatment = answer)
      IS_ON_DIABETES_TREATMENT -> copy(isOnDiabetesTreatment = answer)
      else -> this
    }
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM MedicalHistory WHERE syncStatus = :status")
    fun recordsWithSyncStatus(status: SyncStatus): List<MedicalHistory>

    @Query("""
      SELECT * FROM MedicalHistory
      WHERE syncStatus = :syncStatus
      LIMIT :limit offset :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<MedicalHistory>

    @Query("UPDATE MedicalHistory SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @Query("UPDATE MedicalHistory SET syncStatus = :to WHERE uuid IN (:ids)")
    fun updateSyncStatusForIds(ids: List<UUID>, to: SyncStatus)

    @Query("SELECT * FROM MedicalHistory WHERE uuid = :id LIMIT 1")
    fun getOne(id: UUID): MedicalHistory?

    @Query("SELECT uuid FROM MedicalHistory WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(history: MedicalHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveHistories(histories: List<MedicalHistory>)

    @Query("SELECT COUNT(uuid) FROM MedicalHistory")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM MedicalHistory WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

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

    @Query("""
      SELECT * FROM MedicalHistory
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL
      ORDER BY updatedAt DESC
      LIMIT 1
    """)
    fun historyForPatientImmediate(patientUuid: PatientUuid): MedicalHistory?

    @Query("""
        SELECT (
            CASE
                WHEN (COUNT(uuid) > 0) THEN 1
                ELSE 0
            END
        )
        FROM MedicalHistory
        WHERE updatedAt > :instantToCompare AND syncStatus = :pendingStatus AND patientUuid = :patientUuid
    """)
    fun hasMedicalHistoryForPatientChangedSince(
        patientUuid: UUID,
        instantToCompare: Instant,
        pendingStatus: SyncStatus
    ): Boolean

    @Query("""
      DELETE FROM MedicalHistory
      WHERE deletedAt IS NOT NULL AND syncStatus == 'DONE'
    """)
    fun purgeDeleted()

    @Query(""" SELECT * FROM MedicalHistory """)
    fun getAllMedicalHistories(): List<MedicalHistory>

    @Query("""
        DELETE FROM MedicalHistory
        WHERE 
            uuid NOT IN (
                SELECT MH.uuid FROM MedicalHistory MH
                INNER JOIN Patient P ON P.uuid == MH.patientUuid
            ) AND
            syncStatus == 'DONE'
    """)
    fun deleteWithoutLinkedPatient()

    @Query("""
        DELETE FROM MedicalHistory
        WHERE patientUuid IN (
		        SELECT MH.patientUuid 
			      FROM MedicalHistory MH
			      LEFT JOIN Patient P ON P.uuid == MH.patientUuid
			      WHERE P.uuid IS NULL AND MH.syncStatus == 'DONE'
		    )
    """)
    fun purgeMedicalHistoryWhenPatientIsNull()
  }
}
