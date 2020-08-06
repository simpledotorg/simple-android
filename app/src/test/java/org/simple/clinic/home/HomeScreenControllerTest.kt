package org.simple.clinic.home

import com.nhaarman.mockitokotlin2.doReturn
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
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.util.UUID

class HomeScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  private val screen = mock<HomeScreen>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val clock = TestUserClock()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when home screen is created, then setup the home screen`() {
    // given
    val facility1 = TestData.facility(
        uuid = UUID.fromString("de250445-0ec9-43e4-be33-2a49ca334535"),
        name = "CHC Buchho"
    )
    val facility2 = TestData.facility(
        uuid = UUID.fromString("5b2136b8-11d5-4e20-8703-087281679aee"),
        name = "CHC Nathana"
    )
    val loggedInUser = TestData.loggedInUser(
        uuid = UUID.fromString("751cfb09-92a2-40df-a6b2-b3f82ecd81a1")
    )
    val date = LocalDate.parse("2018-01-01")

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility1, facility2))
    whenever(appointmentRepository.overdueAppointmentsCount(date, facility1)) doReturn Observable.just(3)
    whenever(appointmentRepository.overdueAppointmentsCount(date, facility2)) doReturn Observable.just(0)

    // when
    setupController()

    // then
    verify(screen).setFacility("CHC Buchho")
    verify(screen).setFacility("CHC Nathana")
    verify(screen).showOverdueAppointmentCount(3)
    verify(screen).removeOverdueAppointmentCount()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when facility change button is clicked facility selection screen should open`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("e497355e-723c-4b35-b55a-778a6233b720"),
        name = "CHC Buchho"
    )
    val loggedInUser = TestData.loggedInUser(
        uuid = UUID.fromString("751cfb09-92a2-40df-a6b2-b3f82ecd81a1")
    )
    val date = LocalDate.parse("2018-01-01")

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))
    whenever(appointmentRepository.overdueAppointmentsCount(date, facility)) doReturn Observable.just(0)

    // when
    setupController()
    uiEvents.onNext(HomeFacilitySelectionClicked())

    // then
    verify(screen).setFacility("CHC Buchho")
    verify(screen).removeOverdueAppointmentCount()
    verify(screen).openFacilitySelection()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    clock.setDate(LocalDate.parse("2018-01-01"))

    val controller = HomeScreenController(
        userSession,
        facilityRepository,
        appointmentRepository,
        clock
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}
