package org.simple.clinic.overdue.callresult

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class Outcome : Parcelable {

  object TypeAdapter : SafeEnumTypeAdapter<Outcome>(
      knownMappings = mapOf(
          AgreedToVisit to "agreed_to_visit",
          RemovedFromOverdueList to "removed_from_overdue_list",
          RemindToCallLater to "remind_to_call_later"
      ),
      unknownStringToEnumConverter = ::Unknown,
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  @Parcelize
  object AgreedToVisit : Outcome()

  @Parcelize
  object RemovedFromOverdueList : Outcome()

  @Parcelize
  object RemindToCallLater : Outcome()

  @Parcelize
  data class Unknown(val actualValue: String) : Outcome()

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): Outcome? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(outcome: Outcome?): String? = TypeAdapter.fromEnum(outcome)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): Outcome? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(outcome: Outcome?): String? = TypeAdapter.fromEnum(outcome)
  }
}
