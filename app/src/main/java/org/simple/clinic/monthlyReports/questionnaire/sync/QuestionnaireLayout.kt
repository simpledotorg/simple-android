package org.simple.clinic.monthlyReports.questionnaire.sync

import android.os.Parcelable
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData

@JsonClass(generateAdapter = true)
@Parcelize
data class QuestionnaireLayout(
    @Json(name = "item")
    val items: List<BaseComponentData>,
) : Parcelable {
  @ProvidedTypeConverter
  class RoomTypeConverter(
      private val moshi: Moshi
  ) {
    @TypeConverter
    fun fromBaseComponent(layout: QuestionnaireLayout): String {
      return moshi.adapter(QuestionnaireLayout::class.java).toJson(layout)
    }

    @TypeConverter
    fun toBaseComponent(layoutString: String): QuestionnaireLayout? {
      return moshi.adapter(QuestionnaireLayout::class.java).fromJson(layoutString)
    }
  }
}
