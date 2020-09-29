package org.simple.clinic.login.pin

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Instant
import java.util.UUID

class LoginPinScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<LoginPinScreenUi>()
  private val uiActions = mock<UiActions>()
  private val userSession = mock<UserSession>()
  private val ongoingLoginEntryRepository = mock<OngoingLoginEntryRepository>()

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

  private lateinit var testFixture: MobiusTestFixture<LoginPinModel, LoginPinEvent, LoginPinEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when screen starts, show phone number`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()) doReturn ongoingLoginEntry

    // when
    setupController()

    // then
    verify(ongoingLoginEntryRepository).entryImmediate()
    verifyNoMoreInteractions(ongoingLoginEntryRepository)

    verifyZeroInteractions(userSession)

    verify(ui).showPhoneNumber(phoneNumber)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when back is clicked, the local ongoing login entry must be cleared`() {
    // given
    whenever(ongoingLoginEntryRepository.entryImmediate()) doReturn ongoingLoginEntry

    // when
    setupController()
    uiEvents.onNext(PinBackClicked)

    // then
    verify(ongoingLoginEntryRepository).entryImmediate()
    verify(ongoingLoginEntryRepository).clearLoginEntry()
    verifyNoMoreInteractions(ongoingLoginEntryRepository)

    verifyZeroInteractions(userSession)

    verify(ui).showPhoneNumber(phoneNumber)
    verify(uiActions).goBackToRegistrationScreen()
    verifyNoMoreInteractions(ui, uiActions)
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
    val teleconsultPhoneNumber = "1111111111"
    val capabilities = User.Capabilities(User.CapabilityStatus.Yes)
    val ongoingLoginEntry = ongoingLoginEntry
        .copy(
            pinDigest = pinDigest,
            fullName = fullName,
            registrationFacilityUuid = registrationFacilityUuid,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            teleconsultPhoneNumber = teleconsultPhoneNumber,
            capabilities = capabilities
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
        currentFacilityUuid = registrationFacilityUuid,
        teleconsultPhoneNumber = teleconsultPhoneNumber,
        capabilities = capabilities
    )

    whenever(ongoingLoginEntryRepository.entryImmediate()) doReturn ongoingLoginEntry
    whenever(ongoingLoginEntryRepository.saveLoginEntry(ongoingLoginEntry))
        .thenReturn(Completable.complete())
    whenever(userSession.storeUser(expectedUser))
        .thenReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(LoginPinAuthenticated(ongoingLoginEntry))

    // then
    verify(ongoingLoginEntryRepository).entryImmediate()
    verify(ongoingLoginEntryRepository).saveLoginEntry(ongoingLoginEntry)
    verifyNoMoreInteractions(ongoingLoginEntryRepository)

    verify(userSession).storeUser(user = expectedUser)
    verifyNoMoreInteractions(userSession)

    verify(ui, times(2)).showPhoneNumber(phoneNumber)
    verify(uiActions).openHomeScreen()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = LoginPinEffectHandler(
        schedulersProvider = TestSchedulersProvider.trampoline(),
        userSession = userSession,
        ongoingLoginEntryRepository = ongoingLoginEntryRepository,
        uiActions = uiActions
    )
    val uiRenderer = LoginPinUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = LoginPinModel.create(),
        init = LoginPinInit(),
        update = LoginPinUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
