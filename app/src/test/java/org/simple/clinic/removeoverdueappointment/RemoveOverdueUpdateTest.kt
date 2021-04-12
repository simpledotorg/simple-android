package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.overdue.AppointmentCancelReason
import java.util.UUID

class RemoveOverdueUpdateTest {

  private val updateSpec = UpdateSpec(RemoveOverdueUpdate())
  private val appointmentId = UUID.fromString("05dbff2d-b90a-4e64-a597-332c8cf115ff")
  private val defaultModel = RemoveOverdueModel.create(appointmentId)

  @Test
  fun `when remove appointment reason is clicked, then update the UI`() {
    val selectedReason = RemoveAppointmentReason.AlreadyVisited
    val expectedModel = defaultModel
        .removeAppointmentReasonSelected(selectedReason)

    updateSpec
        .given(defaultModel)
        .whenEvent(RemoveAppointmentReasonSelected(selectedReason))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when patient is migrated, then cancel the appointment`() {
    val cancelReason = AppointmentCancelReason.MovedToPrivatePractitioner

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientMarkedAsMigrated(cancelReason))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CancelAppointment(appointmentId, cancelReason))
        ))
  }

  @Test
  fun `when patient is marked as visited, then go back to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientMarkedAsVisited)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when patient is marked as dead, then go back to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientMarkedAsDead)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }
}
