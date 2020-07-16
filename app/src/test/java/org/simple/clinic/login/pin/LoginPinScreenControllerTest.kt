package org.simple.clinic.login.pin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.UUID

class LoginPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<LoginPinScreen>()
  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val loginUserUuid = UUID.fromString("582099ce-e146-4aa5-8302-85dbbfa1b407")
  private val phoneNumber = "1234567890"
  private val ongoingLoginEntry = TestData.ongoingLoginEntry(
      uuid = loginUserUuid,
      pin = "1234",
      pinDigest = null,
      phoneNumber = phoneNumber,
      fullName = null,
      registrationFacilityUuid = null,
      status = null,
      createdAt = null,
      updatedAt = null
  )

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry())
        .thenReturn(Single.just(ongoingLoginEntry))

    setupController()
    uiEvents.onNext(PinScreenCreated())

    verify(userSession).ongoingLoginEntry()
    verifyNoMoreInteractions(userSession)

    verify(screen).showPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when back is clicked, the local ongoing login entry must be cleared`() {
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    setupController()
    uiEvents.onNext(PinBackClicked())

    verify(userSession).clearOngoingLoginEntry()
    verifyNoMoreInteractions(userSession)

    verify(screen).goBackToRegistrationScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when PIN is verified, save the user and open the home screen`() {
    // given
    val registrationFacilityUuid = UUID.fromString("5314616f-35bb-4a4d-99b1-13f5849de82e")
    val pinDigest = "pin digest"
    val fullName = "Anish Acharya"
    val status = UserStatus.ApprovedForSyncing
    val createdAt = Instant.parse("2019-07-25T06:01:03.325Z")
    val updatedAt = Instant.parse("2019-07-26T06:01:03.325Z")
    val ongoingLoginEntry = ongoingLoginEntry
        .copy(
            pinDigest = pinDigest,
            fullName = fullName,
            registrationFacilityUuid = registrationFacilityUuid,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    val expectedUser = User(
        uuid = loginUserUuid,
        fullName = fullName,
        phoneNumber = phoneNumber,
        pinDigest = pinDigest,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        registrationFacilityUuid = registrationFacilityUuid,
        currentFacilityUuid = registrationFacilityUuid
    )

    whenever(userSession.saveOngoingLoginEntry(ongoingLoginEntry))
        .thenReturn(Completable.complete())
    whenever(userSession.storeUser(expectedUser, registrationFacilityUuid))
        .thenReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(LoginPinAuthenticated(ongoingLoginEntry))

    // then
    verify(userSession).storeUser(user = expectedUser, facilityUuid = registrationFacilityUuid)
    verify(userSession).saveOngoingLoginEntry(ongoingLoginEntry)
    verifyNoMoreInteractions(userSession)

    verify(screen).openHomeScreen()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    val controller = LoginPinScreenController(
        userSession = userSession
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
