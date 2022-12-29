package org.simple.clinic.monthlyReports.questionnaire

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class QuestionnaireComponentType : Parcelable {

  object TypeAdapter : SafeEnumTypeAdapter<QuestionnaireComponentType>(
      knownMappings = mapOf(
          ViewGroup to "group",
          Header to "header",
          SubHeader to "sub_header",
          InputField to "input_field",
      ),
      unknownStringToEnumConverter = ::NotDefined,
      unknownEnumToStringConverter = { (it as NotDefined).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): QuestionnaireComponentType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(componentType: QuestionnaireComponentType?): String? = TypeAdapter.fromEnum(componentType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): QuestionnaireComponentType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(componentType: QuestionnaireComponentType?): String? = TypeAdapter.fromEnum(componentType)
  }

  companion object {
    @VisibleForTesting
    fun random(): QuestionnaireComponentType = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}

@Parcelize
@VisibleForTesting
object ViewGroup : QuestionnaireComponentType()

@Parcelize
@VisibleForTesting
object Header : QuestionnaireComponentType()

@Parcelize
@VisibleForTesting
object SubHeader : QuestionnaireComponentType()

@Parcelize
@VisibleForTesting
object InputField : QuestionnaireComponentType()

@Parcelize
data class NotDefined(val actualValue: String) : QuestionnaireComponentType()
