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
  private val patientId = UUID.fromString("a5856e14-5e81-4776-a50c-59115e1f09de")
  private val defaultModel = RemoveOverdueModel.create(appointmentId, patientId)

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
            hasEffects(GoBackAfterAppointmentRemoval)
        ))
  }

  @Test
  fun `when patient is marked as dead, then go back to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientMarkedAsDead)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackAfterAppointmentRemoval)
        ))
  }

  @Test
  fun `when appointment is cancelled, then go back to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AppointmentMarkedAsCancelled)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackAfterAppointmentRemoval)
        ))
  }

  @Test
  fun `when close button is clicked, then go back`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CloseClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when the appointment is removed because the patient has already visited, the patient must be marked as visited`() {
    val model = defaultModel
        .removeAppointmentReasonSelected(RemoveAppointmentReason.AlreadyVisited)

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkPatientAsVisited(appointmentId))
        ))
  }

  @Test
  fun `when the appointment is removed because the patient has died, the patient must be marked as dead`() {
    val model = defaultModel
        .removeAppointmentReasonSelected(RemoveAppointmentReason.Died)

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkPatientAsDead(patientId = patientId, appointmentId = appointmentId))
        ))
  }

  @Test
  fun `when the appointment is removed because the patient is not responding, the appointment must be cancelled`() {
    val model = defaultModel
        .removeAppointmentReasonSelected(RemoveAppointmentReason.NotResponding)

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CancelAppointment(appointmentUuid = appointmentId, reason = AppointmentCancelReason.PatientNotResponding))
        ))
  }

  @Test
  fun `when the appointment is removed because the phone number is invalid, the appointment must be cancelled`() {
    val model = defaultModel
        .removeAppointmentReasonSelected(RemoveAppointmentReason.PhoneNumberNotWorking)

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CancelAppointment(appointmentUuid = appointmentId, reason = AppointmentCancelReason.InvalidPhoneNumber))
        ))
  }

  @Test
  fun `when the appointment is removed for any other reason, the appointment must be cancelled`() {
    val model = defaultModel
        .removeAppointmentReasonSelected(RemoveAppointmentReason.OtherReason)

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CancelAppointment(appointmentUuid = appointmentId, reason = AppointmentCancelReason.Other))
        ))
  }
}
