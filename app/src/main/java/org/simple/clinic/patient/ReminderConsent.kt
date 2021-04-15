package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class ReminderConsent : Parcelable {

  @Parcelize
  object Granted : ReminderConsent()

  @Parcelize
  object Denied : ReminderConsent()

  @Parcelize
  data class Unknown(val actualValue: String) : ReminderConsent()

  object TypeAdapter : SafeEnumTypeAdapter<ReminderConsent>(
      knownMappings = mapOf(
          Granted to "granted",
          Denied to "denied"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): ReminderConsent? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(status: ReminderConsent?): String? = TypeAdapter.fromEnum(status)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): ReminderConsent? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(status: ReminderConsent?): String? = TypeAdapter.fromEnum(status)
  }
}
