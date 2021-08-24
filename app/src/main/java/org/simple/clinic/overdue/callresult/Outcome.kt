package org.simple.clinic.overdue.callresult

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
}
