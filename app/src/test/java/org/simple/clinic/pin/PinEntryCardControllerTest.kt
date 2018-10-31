package org.simple.clinic.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.applock.ComparisonResult
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class PinEntryCardControllerTest {

  private val screen = mock<PinEntryCardView>()
  private val userSession = mock<UserSession>()
  private val passwordHasher = mock<PasswordHasher>()

  private lateinit var controller: PinEntryCardController
  private val uiEvents = PublishSubject.create<UiEvent>()

  val loggedInUser = PatientMocker.loggedInUser()

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.never<ComparisonResult>())

    controller = PinEntryCardController(userSession, passwordHasher)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    uiEvents.onNext(PinTextChanged("1"))
    uiEvents.onNext(PinTextChanged("12"))
    uiEvents.onNext(PinTextChanged("123"))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(passwordHasher, times(1)).compare(loggedInUser.pinDigest, "1234")
  }

  @Test
  fun `when the PIN is submitted then it should be validated`() {
    uiEvents.onNext(PinTextChanged("1234"))

    verify(passwordHasher).compare(loggedInUser.pinDigest, "1234")
  }

  @Test
  fun `when the PIN is submitted then progress should be shown`() {
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).moveToState(PinEntryCardView.State.Progress)
  }

  @Test
  fun `when PIN validation fails then the progress should be hidden and an error should be shown`() {
    whenever(passwordHasher.compare(any(), eq("1234"))).thenReturn(Single.just(ComparisonResult.DIFFERENT))

    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).moveToState(PinEntryCardView.State.PinEntry)
    verify(screen).showIncorrectPinError()
  }

  @Test
  fun `when PIN validation fails then the PIN should be cleared`() {
    whenever(passwordHasher.compare(any(), eq("1234"))).thenReturn(Single.just(ComparisonResult.DIFFERENT))

    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).clearPin()
  }

  @Test
  fun `when PIN validation succeeds then a success callback should be sent`() {
    whenever(passwordHasher.compare(any(), eq("1234"))).thenReturn(Single.just(ComparisonResult.SAME))

    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).dispatchAuthenticatedCallback()
  }

  @Test
  fun `when the PIN is submitted then any existing validation error should be removed`() {
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).hideError()
  }
}
