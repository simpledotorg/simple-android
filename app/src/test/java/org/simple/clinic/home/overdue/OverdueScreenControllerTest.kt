package org.simple.clinic.home.overdue

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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

@RunWith(JUnitParamsRunner::class)
class OverdueScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val facility = TestData.facility()
  private val user = TestData.loggedInUser()
  private val dateOnClock = LocalDate.parse("2018-01-01")
  private val userClock = TestUserClock(dateOnClock)

  private val controller = OverdueScreenController(
      appointmentRepository = repository,
      userSession = userSession,
      facilityRepository = facilityRepository,
      userClock = userClock
  )

  @Before
  fun setUp() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(user.toOptional()))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(emptyList()))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(emptyList(), false)
    verify(screen).handleEmptyList(true)
    verifyNoMoreInteractions(screen)
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
        TestData.overdueAppointment(isHighRisk = true),
        TestData.overdueAppointment(isHighRisk = false)
    )
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(overdueAppointments))

    // when
    uiEvents.onNext(OverdueScreenCreated())

    // then
    verify(screen).handleEmptyList(false)
    verify(screen).updateList(overdueAppointments, false)
    verifyNoMoreInteractions(screen)
  }
}
