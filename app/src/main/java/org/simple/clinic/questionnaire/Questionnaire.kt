package org.simple.clinic.questionnaire

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaire.component.BaseComponentData
import java.time.Instant
import java.util.UUID

@Entity(tableName = "Questionnaire")
@Parcelize
data class Questionnaire(
    val uuid: UUID,

    @PrimaryKey
    val questionnaire_type: QuestionnaireType,

    val layout: BaseComponentData,

    val deletedAt: Instant?
) : Parcelable {

  @Dao
  interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(questionnaires: List<Questionnaire>)

    @Query("SELECT * FROM Questionnaire")
    fun getAllQuestionnaires(): List<Questionnaire>

    @Query("SELECT * FROM Questionnaire WHERE questionnaire_type == :type LIMIT 1")
    fun getQuestionnaireByType(type: QuestionnaireType): Flowable<Questionnaire>

    @Query("SELECT * FROM Questionnaire WHERE questionnaire_type == :type LIMIT 1")
    fun getQuestionnaireByTypeImmediate(type: QuestionnaireType): Questionnaire

    @Query("SELECT COUNT(questionnaire_type) FROM questionnaire")
    fun count(): Observable<Int>

    @Query("DELETE FROM questionnaire")
    fun clear(): Int
  }
}
