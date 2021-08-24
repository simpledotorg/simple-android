package org.simple.clinic.overdue.callresult

import androidx.room.TypeConverter
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class Outcome {

  object TypeAdapter: SafeEnumTypeAdapter<Outcome>(
      knownMappings = mapOf(
          AgreedToVisit to "agreed_to_visit",
          RemovedFromOverdueList to "removed_from_overdue_list",
          RemindToCallLater to "remind_to_call_later"
      ),
      unknownStringToEnumConverter = ::Unknown,
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  object AgreedToVisit : Outcome()

  object RemovedFromOverdueList: Outcome()

  object RemindToCallLater: Outcome()

  data class Unknown(val actualValue: String): Outcome()

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): Outcome? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(outcome: Outcome?): String? = TypeAdapter.fromEnum(outcome)
  }
}
