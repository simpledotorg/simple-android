package org.simple.clinic.home.overdue

import com.google.common.truth.Truth.assertThat
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
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class OverdueScreenControllerTest {

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val reporter = MockAnalyticsReporter()

  lateinit var controller: OverdueScreenController

  @Before
  fun setUp() {
    controller = OverdueScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    Analytics.addReporter(reporter)
  }

  @Test
  fun `when screen is created, and overdue list is retrieved, show it`() {
    whenever(repository.overdueAppointments()).thenReturn(Observable.just(listOf(PatientMocker.overdueAppointment())))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(any())
    verify(screen).handleEmptyList(false)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(repository.overdueAppointments()).thenReturn(Observable.just(listOf()))

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
    whenever(repository.overdueAppointments()).thenReturn(Observable.just(listOf()))
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
  fun `when "mark patient as agreed to visit" is clicked, then relevant repository method should be called`() {
    val appointmentUuid = UUID.randomUUID()
    whenever(repository.markAsAgreedToVisit(appointmentUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(AgreedToVisitClicked(appointmentUuid))

    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the screen is opened, the viewed patient analytics event must be sent`() {
    val patientUuid = UUID.randomUUID()
    uiEvents.onNext(AppointmentExpanded(patientUuid))

    val expectedEvent = MockAnalyticsReporter.Event("ViewedPatient", mapOf(
        "patientId" to patientUuid.toString(),
        "from" to "Overdue"
    ))
    assertThat(reporter.receivedEvents).contains(expectedEvent)
  }

  fun `params for permission-result dialer-method`(): Array<Array<Any>> {
    return arrayOf(
        arrayOf(RuntimePermissionResult.GRANTED, false),
        arrayOf(RuntimePermissionResult.DENIED, true),
        arrayOf(RuntimePermissionResult.NEVER_ASK_AGAIN, true)
    )
  }
}
