package org.simple.clinic.overdue.callresult

sealed class Outcome {

  object AgreedToVisit : Outcome()

  object RemovedFromOverdueList: Outcome()

  object RemindToCallLater: Outcome()

  data class Unknown(val actualValue: String): Outcome()
}
