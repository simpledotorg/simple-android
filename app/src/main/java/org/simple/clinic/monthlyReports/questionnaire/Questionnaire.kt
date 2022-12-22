package org.simple.clinic.monthlyReports.questionnaire

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnairePayload
import org.simple.clinic.util.Unicode
import java.util.UUID

@Entity(tableName = "Questionnaire")
@Parcelize
data class Questionnaire(
    val uuid: UUID,

    @PrimaryKey
    val questionnaire_type: QuestionnaireType,

    val layout: String,
) : Parcelable {

  fun toPayload(): QuestionnairePayload {
    return QuestionnairePayload(
        uuid = uuid,
        questionnaireType = questionnaire_type,
        layout = layout)
  }

  override fun toString(): String {
    return "Questionnaire(${Unicode.redacted})"
  }

  @Dao
  interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(questionnaires: List<Questionnaire>)

    @Query("SELECT * FROM Questionnaire")
    fun getAllQuestionnaires(): List<Questionnaire>

    @Query("SELECT * FROM Questionnaire WHERE questionnaire_type == :type")
    fun getQuestionnaireByType(type: QuestionnaireType): Questionnaire

    @Query("SELECT COUNT(questionnaire_type) FROM questionnaire")
    fun count(): Observable<Int>

    @Query("DELETE FROM questionnaire")
    fun clearData(): Int
  }
}
