package org.simple.clinic.home.overdue.removepatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.overdue.Appointment.CancelReason.DEAD
import org.simple.clinic.overdue.Appointment.CancelReason.MOVED
import org.simple.clinic.overdue.Appointment.CancelReason.OTHER
import org.simple.clinic.overdue.Appointment.CancelReason.PATIENT_NOT_RESPONDING
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RemoveAppointmentSheetControllerTest {

  private val sheet = mock<RemoveAppointmentSheet>()
  private val repository = mock<AppointmentRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val appointmentUuid = UUID.randomUUID()

  private val patientRepository = mock<PatientRepository>()

  lateinit var controller: RemoveAppointmentSheetController

  @Before
  fun setUp() {
    controller = RemoveAppointmentSheetController(repository, patientRepository)
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  fun `when done is clicked, and no reason is selected, nothing should happen`() {
    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(sheet, never()).enableDoneButton()
    verify(sheet, never()).closeSheet()
  }

  @Test
  fun `when done is clicked, and reason is "already visited", then repository should update and sheet should close`() {
    whenever(repository.markAsAlreadyVisited(appointmentUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(CancelReasonClicked(DEAD))
    uiEvents.onNext(AlreadyVisitedReasonClicked)
    uiEvents.onNext(CancelReasonClicked(PATIENT_NOT_RESPONDING))
    uiEvents.onNext(CancelReasonClicked(OTHER))
    uiEvents.onNext(AlreadyVisitedReasonClicked)
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).cancelWithReason(any(), any())

    val inOrder = inOrder(sheet, repository)
    inOrder.verify(sheet, times(5)).enableDoneButton()
    inOrder.verify(repository).markAsAlreadyVisited(appointmentUuid)
    inOrder.verify(sheet).closeSheet()
  }

  @Test
  fun `when done is clicked, and reason is "Patient dead", then patient repository should be updated`(){
    whenever(repository.cancelWithReason(appointmentUuid, DEAD)).thenReturn(Completable.complete())
    val patientUuid = UUID.randomUUID()
    whenever(patientRepository.updatePatientStatusToDead(patientUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(AlreadyVisitedReasonClicked)
    uiEvents.onNext(CancelReasonClicked(PATIENT_NOT_RESPONDING))
    uiEvents.onNext(CancelReasonClicked(DEAD))
    uiEvents.onNext(PatientDeadClicked(patientUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).markAsAlreadyVisited(any())

    val inOrder = inOrder(sheet, repository, patientRepository)
    inOrder.verify(sheet, times(4)).enableDoneButton()
    inOrder.verify(patientRepository).updatePatientStatusToDead(patientUuid)
    inOrder.verify(repository).cancelWithReason(appointmentUuid, DEAD)
    inOrder.verify(sheet).closeSheet()
  }

  @Test
  fun `when done is clicked, and a cancel reason is selected, then repository should update and sheet should close`() {
    whenever(repository.cancelWithReason(appointmentUuid, MOVED)).thenReturn(Completable.complete())

    uiEvents.onNext(RemoveAppointmentSheetCreated(appointmentUuid))
    uiEvents.onNext(RemoveReasonDoneClicked)
    uiEvents.onNext(AlreadyVisitedReasonClicked)
    uiEvents.onNext(CancelReasonClicked(PATIENT_NOT_RESPONDING))
    uiEvents.onNext(AlreadyVisitedReasonClicked)
    uiEvents.onNext(CancelReasonClicked(MOVED))
    uiEvents.onNext(RemoveReasonDoneClicked)

    verify(repository, never()).markAsAlreadyVisited(any())

    val inOrder = inOrder(sheet, repository)
    inOrder.verify(sheet, times(4)).enableDoneButton()
    inOrder.verify(repository).cancelWithReason(appointmentUuid, MOVED)
    inOrder.verify(sheet).closeSheet()
  }
}
