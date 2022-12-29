package org.simple.clinic.util.room

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireLayout
import javax.inject.Inject

@ProvidedTypeConverter
class QuestionnaireLayoutRoomTypeConverter @Inject constructor(
    private val moshi: Moshi
) {
  @TypeConverter
  fun fromQuestionnaireLayout(layout: QuestionnaireLayout): String {
    return moshi.adapter(QuestionnaireLayout::class.java).toJson(layout)
  }

  @TypeConverter
  fun toQuestionnaireLayout(layoutString: String): QuestionnaireLayout? {
    return moshi.adapter(QuestionnaireLayout::class.java).fromJson(layoutString)
  }
}
