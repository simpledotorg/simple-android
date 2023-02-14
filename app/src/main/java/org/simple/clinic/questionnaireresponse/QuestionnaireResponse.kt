package org.simple.clinic.questionnaireresponse

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponsePayload
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity(tableName = "QuestionnaireResponse")
@Parcelize
data class QuestionnaireResponse(
    @PrimaryKey
    val uuid: UUID,

    val questionnaireId: UUID,

    val questionnaireType: QuestionnaireType,

    val facilityId: UUID,

    val lastUpdatedByUserId: UUID,

    val content: Map<String, @RawValue Any>,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable {

  fun toPayload() = QuestionnaireResponsePayload(
      uuid = uuid,
      questionnaireId = questionnaireId,
      questionnaireType = questionnaireType,
      facilityId = facilityId,
      lastUpdatedByUserId = lastUpdatedByUserId,
      createdAt = timestamps.createdAt,
      updatedAt = timestamps.updatedAt,
      deletedAt = timestamps.deletedAt,
      content = content
  )

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(questionnaireResponses: List<QuestionnaireResponse>)

    @Query("SELECT * FROM QuestionnaireResponse WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): QuestionnaireResponse?

    @Query("SELECT * FROM QuestionnaireResponse WHERE questionnaireType = :type")
    fun getByQuestionnaireType(type: QuestionnaireType): List<QuestionnaireResponse>

    @Update
    fun updateQuestionnaireResponse(questionnaireResponse: QuestionnaireResponse)

    @Query("SELECT * FROM QuestionnaireResponse WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): List<QuestionnaireResponse>

    @Query("""
      SELECT * FROM QuestionnaireResponse
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<QuestionnaireResponse>

    @Query("UPDATE QuestionnaireResponse SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE QuestionnaireResponse SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatusForIds(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT uuid FROM QuestionnaireResponse WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("SELECT COUNT(uuid) FROM QuestionnaireResponse")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM QuestionnaireResponse WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("DELETE FROM QuestionnaireResponse")
    fun clear()

    @Query("""
      DELETE FROM QuestionnaireResponse
      WHERE deletedAt IS NOT NULL AND syncStatus == 'DONE'
    """)
    fun purgeDeleted()

    @Query(""" SELECT * FROM QuestionnaireResponse """)
    fun getAllQuestionnaireResponse(): List<QuestionnaireResponse>
  }
}
