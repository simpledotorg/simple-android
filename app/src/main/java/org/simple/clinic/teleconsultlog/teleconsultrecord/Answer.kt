package org.simple.clinic.teleconsultlog.teleconsultrecord

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class Answer : Parcelable {

  @Parcelize
  object Yes : Answer()

  @Parcelize
  object No : Answer()

  @Parcelize
  data class Unknown(val actualValue: String) : Answer()

  object TypeAdapter : SafeEnumTypeAdapter<Answer>(
      knownMappings = mapOf(
          Yes to "yes",
          No to "no"
      ),
      unknownEnumToStringConverter = { (it as Unknown).actualValue },
      unknownStringToEnumConverter = { Unknown(it) }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): Answer? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(answer: Answer?): String? = TypeAdapter.fromEnum(answer)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): Answer? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(answer: Answer?): String? = TypeAdapter.fromEnum(answer)
  }
}
