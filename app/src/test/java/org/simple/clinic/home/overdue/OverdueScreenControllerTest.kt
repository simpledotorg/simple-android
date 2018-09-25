package org.simple.clinic.home.overdue

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class OverdueScreenControllerTest {

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val appointmentRepo = mock<AppointmentRepository>()

  lateinit var controller: OverdueScreenController

  @Before
  fun setUp() {
    controller = OverdueScreenController(appointmentRepo)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created, and overdue list is retrieved, show it`() {
    whenever(appointmentRepo.overdueAppointments()).thenReturn(Observable.just(listOf(PatientMocker.overdueAppointment())))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(any())
    verify(screen).handleEmptyList(false)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(appointmentRepo.overdueAppointments()).thenReturn(Observable.just(listOf()))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(any())
    verify(screen).handleEmptyList(true)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when call button is clicked, call permission should be requested`() {
    uiEvents.onNext(CallPatientClicked("99999"))

    verify(screen).requestCallPermission()
  }

  @Test
  @Parameters(method = "params for permission-result dialer-method")
  fun `when call button is clicked, and permission result is received, call should be made using relevant method`(
      result: RuntimePermissionResult,
      shouldUseDialer: Boolean
  ) {
    whenever(appointmentRepo.overdueAppointments()).thenReturn(Observable.just(listOf()))
    val phone = "99999"

    uiEvents.onNext(CallPatientClicked(phone))
    uiEvents.onNext(CallPhonePermissionChanged(result))

    verify(screen).requestCallPermission()
    when (shouldUseDialer) {
      true -> verify(screen).callPatientUsingDialer(phone)
      false -> verify(screen).callPatientWithoutUsingDialer(phone)
    }
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when "remind to call later" is clicked, appointment reminder sheet should open`() {
    val appointmentUuid = UUID.randomUUID()
    uiEvents.onNext(RemindToCallLaterClicked(appointmentUuid))

    verify(screen).showAppointmentReminderSheet(appointmentUuid)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when "mark patient as agreed to visit" is click`() {
    val appointmentUuid = UUID.randomUUID()

    whenever(appointmentRepo.agreedToVisit(appointmentUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(AgreedToVisitClicked(appointmentUuid))

    verifyNoMoreInteractions(screen)
  }

  fun `params for permission-result dialer-method`(): Array<Array<Any>> {
    return arrayOf(
        arrayOf(RuntimePermissionResult.GRANTED, false),
        arrayOf(RuntimePermissionResult.DENIED, true),
        arrayOf(RuntimePermissionResult.NEVER_ASK_AGAIN, true)
    )
  }
}
