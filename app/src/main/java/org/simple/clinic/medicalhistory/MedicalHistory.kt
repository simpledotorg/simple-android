package org.simple.clinic.medicalhistory

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import io.reactivex.Flowable
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RoomEnumTypeConverter
import org.simple.clinic.util.SafeEnumTypeAdapter
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

  sealed class Answer {

    object Yes : Answer()

    object No: Answer()

    object Unanswered: Answer()

    data class Unknown(val actualValue: String): Answer()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    object TypeAdapter: SafeEnumTypeAdapter<Answer>(
        knownMappings = mapOf(
            Yes to "yes",
            No to "no",
            // The actual value of the Unanswered enum should be "unanswered",
            // but the current api representation is "unknown". This is a little
            // confusing because we use Unknown as the convention for values which
            // this app version doesn't know about yet.
            // TODO 16-Jul-19 : See if it's possible to change this in a future api version
            Unanswered to "unknown"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actualValue }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?): Answer? = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(answer: Answer): String? = TypeAdapter.fromEnum(answer)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun fromJson(value: String?): Answer? = TypeAdapter.toEnum(value)

      @ToJson
      fun toJson(answer: Answer): String? = TypeAdapter.fromEnum(answer)
    }
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
    ): Flowable<Boolean>
  }
}
