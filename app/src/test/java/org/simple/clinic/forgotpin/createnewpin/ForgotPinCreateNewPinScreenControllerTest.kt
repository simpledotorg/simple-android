package org.simple.clinic.forgotpin.createnewpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class ForgotPinCreateNewPinScreenControllerTest {

  private val screen = mock<ForgotPinCreateNewPinScreen>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()

  private val loggedInUser = PatientMocker.loggedInUser()
  private val facility = PatientMocker.facility()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: ForgotPinCreateNewPinScreenController

  @Before
  fun setUp() {
    controller = ForgotPinCreateNewPinScreenController(userSession, facilityRepository)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just((loggedInUser)))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(facility))

    uiEvents.compose(controller)
        .subscribe { it.invoke(screen) }
  }

  @Test
  fun `on start, the logged in user's full name must be shown`() {
    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserName(loggedInUser.fullName)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    uiEvents.onNext(ScreenCreated())

    verify(screen).showFacility(facility.name)
  }


}
