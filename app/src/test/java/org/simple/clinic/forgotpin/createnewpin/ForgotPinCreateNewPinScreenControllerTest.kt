package org.simple.clinic.forgotpin.createnewpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
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

  @Test
  fun `when an incomplete PIN is submitted, an error must be shown`() {
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("11"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    verify(screen, times(3)).showInvalidPinError()
  }

  @Test
  fun `when a complete PIN is submitted, the confirm PIN screen must be shown`() {
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    verify(screen).showConfirmPinScreen("1111")
  }

  @Test
  fun `when the PIN text changes, any error must be hidden`() {
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1"))
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("11"))

    verify(screen, times(2)).hideInvalidPinError()
  }

  @Test
  fun `when the facility name is clicked then facility change screen should be shown`() {
    uiEvents.onNext(ForgotPinCreateNewPinFacilityClicked)

    verify(screen).openFacilityChangeScreen()
  }
}
