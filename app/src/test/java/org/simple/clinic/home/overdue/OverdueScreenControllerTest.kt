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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
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
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val facility = PatientMocker.facility()
  private val user = PatientMocker.loggedInUser()
  private val userClock = TestUserClock(LocalDate.parse("2018-01-01"))

  private val controller = OverdueScreenController(
      appointmentRepository = repository,
      userSession = userSession,
      facilityRepository = facilityRepository,
      userClock = userClock
  )

  @Before
  fun setUp() {
    whenever(maskedPhoneCaller.secureCall(any(), any())).thenReturn(Completable.complete())
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(user.toOptional()))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    configStream.onNext(PhoneNumberMaskerConfig(proxyPhoneNumber = "0123456789"))

    Analytics.addReporter(reporter)
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(repository.overdueAppointments(facility))
        .thenReturn(Observable.just(emptyList()))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(emptyList())
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
    whenever(repository.markAsAgreedToVisit(appointmentUuid, userClock)).thenReturn(Completable.complete())

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
  fun `when showPhoneMaskBottomSheet config is true and call patient is clicked then open phone mask bottom sheet`() {
    val patientUuid = UUID.randomUUID()

    uiEvents.onNext(CallPatientClicked(patientUuid))

    verify(screen).openPhoneMaskBottomSheet(patientUuid)
  }

  @Test
  fun `when screen is created then the overdue appointments must be displayed`() {
    // given
    val overdueAppointments = listOf(
        PatientMocker.overdueAppointment(riskLevel = OverdueAppointment.RiskLevel.HIGHEST),
        PatientMocker.overdueAppointment(riskLevel = OverdueAppointment.RiskLevel.LOW),
        PatientMocker.overdueAppointment(riskLevel = OverdueAppointment.RiskLevel.NONE)
    )
    whenever(repository.overdueAppointments(facility))
        .thenReturn(Observable.just(overdueAppointments))

    // when
    uiEvents.onNext(OverdueScreenCreated())

    // then
    verify(screen).handleEmptyList(false)
    verify(screen).updateList(overdueAppointments)
    verifyNoMoreInteractions(screen)
  }
}
