package org.simple.clinic.teleconsultlog.teleconsultrecord

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class TeleconsultationType {

  object Audio : TeleconsultationType()

  object Video : TeleconsultationType()

  object Message : TeleconsultationType()

  data class Unknown(val actualValue: String) : TeleconsultationType()

  object TypeAdapter : SafeEnumTypeAdapter<TeleconsultationType>(
      knownMappings = mapOf(
          Audio to "audio",
          Video to "video",
          Message to "message"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): TeleconsultationType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(type: TeleconsultationType?): String? = TypeAdapter.fromEnum(type)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): TeleconsultationType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(type: TeleconsultationType?): String? = TypeAdapter.fromEnum(type)
  }
}
