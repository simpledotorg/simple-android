package org.simple.clinic.home.overdue

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.util.UUID

class OverdueScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val facility = TestData.facility(uuid = UUID.fromString("f4430584-eeaf-4352-b1f5-c21cc96faa6c"))
  private val user = TestData.loggedInUser(uuid = UUID.fromString("977c9f22-c333-477f-826b-00fa601f16ab"))
  private val dateOnClock = LocalDate.parse("2018-01-01")
  private val userClock = TestUserClock(dateOnClock)

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(emptyList()))

    setupController()

    verify(screen).updateList(emptyList(), false)
    verify(screen).handleEmptyList(true)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when showPhoneMaskBottomSheet config is true and call patient is clicked then open phone mask bottom sheet`() {
    val patientUuid = UUID.fromString("55daf914-82df-4c41-ba1b-131216fed30c")
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            patientUuid = patientUuid,
            appointmentUuid = UUID.fromString("5ee43e06-44fd-413e-a222-23c79f0b0f3a"),
            phoneNumberUuid = UUID.fromString("1f8c184c-09b2-408c-a87e-96a144b7cf22"),
            facilityUuid = facility.uuid,
            isHighRisk = true
        )
    )
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(overdueAppointments))

    setupController()
    uiEvents.onNext(CallPatientClicked(patientUuid))

    verify(screen).handleEmptyList(false)
    verify(screen).updateList(overdueAppointments, false)
    verify(screen).openPhoneMaskBottomSheet(patientUuid)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when screen is created then the overdue appointments must be displayed`() {
    // given
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("d79923ea-bedd-484a-ae28-465be3051f45"),
            appointmentUuid = UUID.fromString("5ee43e06-44fd-413e-a222-23c79f0b0f3a"),
            phoneNumberUuid = UUID.fromString("1f8c184c-09b2-408c-a87e-96a144b7cf22"),
            facilityUuid = facility.uuid,
            isHighRisk = true
        ),
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("d89a1040-e732-4dd9-b975-eea7b9c53414"),
            appointmentUuid = UUID.fromString("3e4a7da3-1921-4260-93bd-ff9f1dc98476"),
            phoneNumberUuid = UUID.fromString("50c1fb5c-0406-4e98-8150-696accdab113"),
            facilityUuid = facility.uuid,
            isHighRisk = false
        )
    )
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(overdueAppointments))

    // when
    setupController()

    // then
    verify(screen).handleEmptyList(false)
    verify(screen).updateList(overdueAppointments, false)
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(user.toOptional()))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    val controller = OverdueScreenController(
        appointmentRepository = repository,
        userSession = userSession,
        facilityRepository = facilityRepository,
        userClock = userClock
    )
    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(OverdueScreenCreated())
  }
}
