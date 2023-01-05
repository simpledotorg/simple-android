package org.simple.clinic.monthlyReports.questionnaire.component.properties

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class InputFieldType : Parcelable {

  object TypeAdapter : SafeEnumTypeAdapter<InputFieldType>(
      knownMappings = mapOf(
          Integer to "integer"
      ),
      unknownStringToEnumConverter = ::UnknownType,
      unknownEnumToStringConverter = { (it as UnknownType).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): InputFieldType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(inputFieldType: InputFieldType?): String? = TypeAdapter.fromEnum(inputFieldType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): InputFieldType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(inputFieldType: InputFieldType?): String? = TypeAdapter.fromEnum(inputFieldType)
  }

  companion object {
    @VisibleForTesting
    fun random(): InputFieldType = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}

@Parcelize
object Integer : InputFieldType()

@Parcelize
data class UnknownType(val actualValue: String) : InputFieldType()
