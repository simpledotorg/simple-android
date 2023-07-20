package org.simple.clinic.questionnaire.component.properties

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed interface InputFieldViewType : Parcelable {

  object TypeAdapter : SafeEnumTypeAdapter<InputFieldViewType>(
      knownMappings = mapOf(
          MonthYearPicker to "month_year_picker",
          InputField to "input_field"
      ),
      unknownStringToEnumConverter = ::UnknownType,
      unknownEnumToStringConverter = { (it as UnknownType).actualValue }
  )

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): InputFieldViewType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(inputFieldType: InputFieldViewType?): String? = TypeAdapter.fromEnum(inputFieldType)
  }

  @Parcelize
  object InputField : InputFieldViewType

  @Parcelize
  object MonthYearPicker : InputFieldViewType

  @Parcelize
  data class UnknownType(val actualValue: String) : InputFieldViewType
}
