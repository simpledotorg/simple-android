package org.simple.clinic.teleconsultlog.teleconsultrecord

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class TeleconsultStatus : Parcelable {

  @Parcelize
  object Yes : TeleconsultStatus() {
    override fun toString(): String = "Yes"
  }

  @Parcelize
  object No : TeleconsultStatus() {
    override fun toString(): String = "No"
  }

  @Parcelize
  object StillWaiting : TeleconsultStatus() {
    override fun toString(): String = "waiting"
  }

  @Parcelize
  data class Unknown(val actualValue: String) : TeleconsultStatus()

  object TypeAdapter : SafeEnumTypeAdapter<TeleconsultStatus>(
      knownMappings = mapOf(
          Yes to "yes",
          No to "no",
          StillWaiting to "waiting"
      ),
      unknownEnumToStringConverter = { (it as Unknown).actualValue },
      unknownStringToEnumConverter = { Unknown(it) }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): TeleconsultStatus? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(status: TeleconsultStatus?): String? = TypeAdapter.fromEnum(status)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): TeleconsultStatus? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(status: TeleconsultStatus?): String? = TypeAdapter.fromEnum(status)
  }
}

