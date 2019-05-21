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
import io.reactivex.subjects.Subject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class OverdueScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val maskedPhoneCaller = mock<PhoneCaller>()
  private val reporter = MockAnalyticsReporter()
  private val configStream: Subject<PhoneNumberMaskerConfig> = PublishSubject.create()

  private lateinit var controller: OverdueScreenController

  @Before
  fun setUp() {
    whenever(maskedPhoneCaller.secureCall(any(), any())).thenReturn(Completable.complete())

    controller = OverdueScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    configStream.onNext(PhoneNumberMaskerConfig(proxyPhoneNumber = "0123456789"))

    Analytics.addReporter(reporter)
  }

  @Test
  fun `when screen is created, and overdue list is retrieved, show it`() {
    whenever(repository.overdueAppointments())
        .thenReturn(Observable.just(listOf(PatientMocker.overdueAppointment(riskLevel = OverdueAppointment.RiskLevel.HIGHEST))))

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

  @Test
  fun `when masking is enabled then the header should be added`() {
    val overdueAppointment = PatientMocker.overdueAppointment(riskLevelIndex = 0)
    whenever(repository.overdueAppointments()).thenReturn(Observable.just(listOf(overdueAppointment)))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(listOf(
        OverdueListItem.Patient(
            appointmentUuid = overdueAppointment.appointment.uuid,
            patientUuid = overdueAppointment.appointment.patientUuid,
            name = overdueAppointment.fullName,
            gender = overdueAppointment.gender,
            age = 30,
            phoneNumber = null,
            bpSystolic = overdueAppointment.bloodPressure.systolic,
            bpDiastolic = overdueAppointment.bloodPressure.diastolic,
            bpDaysAgo = 0,
            overdueDays = 0,
            isAtHighRisk = true
        )
    ))
  }

  @Test
  fun `when masking is disabled then the header should be not shown`() {
    val controller = OverdueScreenController(repository)

    val uiEvents = PublishSubject.create<UiEvent>()
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    val overdueAppointment = PatientMocker.overdueAppointment(riskLevelIndex = 0)
    whenever(repository.overdueAppointments()).thenReturn(Observable.just(listOf(overdueAppointment)))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(listOf(
        OverdueListItem.Patient(
            appointmentUuid = overdueAppointment.appointment.uuid,
            patientUuid = overdueAppointment.appointment.patientUuid,
            name = overdueAppointment.fullName,
            gender = overdueAppointment.gender,
            age = 30,
            phoneNumber = null,
            bpSystolic = overdueAppointment.bloodPressure.systolic,
            bpDiastolic = overdueAppointment.bloodPressure.diastolic,
            bpDaysAgo = 0,
            overdueDays = 0,
            isAtHighRisk = true
        )
    ))
  }

  @Test
  fun `when showPhoneMaskBottomSheet config is true and call patient is clicked then open phone mask bottom sheet`() {
    val patientUuid = UUID.randomUUID()

    uiEvents.onNext(CallPatientClicked(patientUuid))

    verify(screen).openPhoneMaskBottomSheet(patientUuid)
  }
}
