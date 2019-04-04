package org.simple.clinic.home

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class HomeScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: HomeScreenController

  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()

  private val screen = mock<HomeScreen>()

  @Before
  fun setUp() {
    controller = HomeScreenController(
        userSession,
        facilityRepository
    )

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when home screen is created current selected facility should be set`() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser()))
    val facility1 = PatientMocker.facility(name = "CHC Buchho")
    val facility2 = PatientMocker.facility(name = "CHC Nathana")
    whenever(facilityRepository.currentFacility(any<User>()))
        .thenReturn(Observable.just(facility1, facility2))

    uiEvents.onNext(ScreenCreated())

    verify(screen).setFacility("CHC Buchho")
    verify(screen).setFacility("CHC Nathana")
  }

  @Test
  fun `when facility change button is clicked facility selection screen should open`() {
    uiEvents.onNext(HomeFacilitySelectionClicked())
    verify(screen).openFacilitySelection()
  }
}
