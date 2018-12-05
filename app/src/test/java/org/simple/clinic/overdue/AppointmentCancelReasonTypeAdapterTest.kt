package org.simple.clinic.overdue

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AppointmentCancelReasonTypeAdapterTest {

  @Test
  @Parameters(method = "reason types to json keys")
  fun `reason types should be correctly serialized to their json keys`(
      reason: AppointmentCancelReason,
      expectedJsonKey: String
  ) {
    val serialized = AppointmentCancelReason.TypeAdapter.fromEnum(reason)
    assertThat(serialized).isEqualTo(expectedJsonKey)
  }

  @Suppress("unused")
  fun `reason types to json keys`(): List<List<Any>> {
    return listOf(
        listOf(AppointmentCancelReason.PatientNotResponding, "not_responding"),
        listOf(AppointmentCancelReason.Moved, "moved"),
        listOf(AppointmentCancelReason.Dead, "dead"),
        listOf(AppointmentCancelReason.Other, "other")
    )
  }

  @Test
  @Parameters(method = "json keys to reason types")
  fun `reason types should be correctly deserialized from their json keys`(
      jsonKey: String,
      expectedReason: AppointmentCancelReason
  ) {
    val deserialized = AppointmentCancelReason.TypeAdapter.toEnum(jsonKey)
    assertThat(deserialized).isEqualTo(expectedReason)
  }

  @Suppress("unused")
  fun `json keys to reason types`(): List<List<Any>> {
    return listOf(
        listOf("not_responding", AppointmentCancelReason.PatientNotResponding),
        listOf("moved", AppointmentCancelReason.Moved),
        listOf("dead", AppointmentCancelReason.Dead),
        listOf("other", AppointmentCancelReason.Other)
    )
  }

  @Test
  @Parameters("abducted_by_joker", "disapparated_in_hogwarts")
  fun `unknown reason enum should be serialized to its actual value`(reason: String) {
    val unknownReason = AppointmentCancelReason.Unknown(actualValue = reason)
    val serialized = AppointmentCancelReason.TypeAdapter.fromEnum(unknownReason)

    assertThat(serialized).isEqualTo(unknownReason.actualValue)
  }

  @Test
  fun `unknown reason strings should be deserialized correctly`() {
    val unknownReasonString = "abducted_by_joker"
    val deserialized = AppointmentCancelReason.TypeAdapter.toEnum(unknownReasonString)

    assertThat(deserialized).isEqualTo(AppointmentCancelReason.Unknown(actualValue = unknownReasonString))
  }
}
