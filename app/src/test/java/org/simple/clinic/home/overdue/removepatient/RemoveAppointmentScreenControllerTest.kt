package org.simple.clinic.home.overdue.removepatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.Dead
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.MovedToPrivatePractitioner
import org.simple.clinic.overdue.AppointmentCancelReason.Other
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.AppointmentCancelReason.TransferredToAnotherPublicHospital
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class RemoveAppointmentScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<RemoveAppointmentScreen>()
  private val repository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()

  private val appointmentUuid = UUID.randomUUID()
  private val uiEvents = PublishSubject.create<UiEvent>()

  lateinit var controller: RemoveAppointmentScreenController

  @Before
  fun setUp() {
    controller = RemoveAppointmentScreenController(repository, patientRepository)
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  fun `when done is clicked, and no reason is selected, nothing should happen`() {
    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(sheet, never()).enableDoneButton()
    verify(sheet, never()).closeScreen()
  }

  @Test
  fun `when done is clicked, and reason is "Patient dead", then patient repository should be updated`() {
    whenever(repository.cancelWithReason(appointmentUuid, Dead)).thenReturn(Completable.complete())
    val patientUuid = UUID.randomUUID()
    whenever(patientRepository.updatePatientStatusToDead(patientUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(CancelReasonClicked(PatientNotResponding))
    uiEvents.onNext(CancelReasonClicked(Dead))
    uiEvents.onNext(PatientDeadClicked(patientUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).markAsAlreadyVisited(any())

    val inOrder = inOrder(sheet, repository, patientRepository)
    inOrder.verify(sheet, atLeastOnce()).enableDoneButton()
    inOrder.verify(patientRepository).updatePatientStatusToDead(patientUuid)
    inOrder.verify(repository).cancelWithReason(appointmentUuid, Dead)
    inOrder.verify(sheet).closeScreen()
  }

  @Test
  @Parameters(method = "params for cancel reasons")
  fun `when done is clicked, and a cancel reason is selected, then repository should update and sheet should close`(
      finallyClickedCancelReason: AppointmentCancelReason
  ) {
    whenever(repository.cancelWithReason(appointmentUuid, finallyClickedCancelReason)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(CancelReasonClicked(PatientNotResponding))
    uiEvents.onNext(CancelReasonClicked(InvalidPhoneNumber))
    uiEvents.onNext(CancelReasonClicked(Other))
    uiEvents.onNext(CancelReasonClicked(finallyClickedCancelReason))
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).markAsAlreadyVisited(any())

    val inOrder = inOrder(sheet, repository)
    inOrder.verify(sheet, atLeastOnce()).enableDoneButton()
    inOrder.verify(repository).cancelWithReason(appointmentUuid, finallyClickedCancelReason)
    inOrder.verify(sheet).closeScreen()
  }

  @Suppress("Unused")
  private fun `params for cancel reasons`(): List<AppointmentCancelReason> {
    return listOf(
        PatientNotResponding,
        InvalidPhoneNumber,
        TransferredToAnotherPublicHospital,
        PatientNotResponding,
        MovedToPrivatePractitioner,
        Other)
  }

  @Test
  fun `when done is clicked, and a "Patient has already visited" reason is selected, then appointment should be marked as visited`() {
    whenever(repository.markAsAlreadyVisited(appointmentUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(PatientAlreadyVisitedClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).cancelWithReason(any(), any())

    val inOrder = inOrder(sheet, repository)
    inOrder.verify(sheet, atLeastOnce()).enableDoneButton()
    inOrder.verify(repository).markAsAlreadyVisited(appointmentUuid)
    inOrder.verify(sheet).closeScreen()
  }
}
