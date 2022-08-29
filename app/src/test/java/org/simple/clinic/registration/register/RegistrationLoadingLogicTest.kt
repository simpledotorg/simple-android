package org.simple.clinic.registration.register

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.user.registeruser.RegistrationResult
import org.simple.clinic.user.registeruser.RegistrationResult.NetworkError
import org.simple.clinic.user.registeruser.RegistrationResult.Success
import org.simple.clinic.user.registeruser.RegistrationResult.UnexpectedError
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Instant
import java.util.UUID

class RegistrationLoadingLogicTest {

  private val ui = mock<RegistrationLoadingUi>()
  private val uiActions = mock<RegistrationLoadingUiActions>()
  private val registerUser = mock<RegisterUser>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val clock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val passwordHasher = JavaHashPasswordHasher()

  private val facility = TestData.facility(uuid = UUID.fromString("6bf38b14-ae29-4b02-ad26-8340bbe6d861"))

  private val registrationEntry = TestData.ongoingRegistrationEntry(
      uuid = UUID.fromString("fe1786be-5725-45b5-a6aa-e9ce0f99f794"),
      fullName = "Anish Acharya",
      phoneNumber = "1234567890",
      pin = "1111",
      registrationFacility = facility
  )

  private val user = User(
      uuid = registrationEntry.uuid!!,
      loggedInStatus = LOGGED_IN,
      status = WaitingForApproval,
      fullName = registrationEntry.fullName!!,
      phoneNumber = registrationEntry.phoneNumber!!,
      pinDigest = passwordHasher.hash(registrationEntry.pin!!),
      createdAt = Instant.now(clock),
      updatedAt = Instant.now(clock),
      registrationFacilityUuid = registrationEntry.facilityId!!,
      currentFacilityUuid = registrationEntry.facilityId!!,
      teleconsultPhoneNumber = null,
      capabilities = null
  )

  private lateinit var testFixture: MobiusTestFixture<RegistrationLoadingModel, RegistrationLoadingEvent, RegistrationLoadingEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when retry button is clicked, then user registration should be attempted again`() {
    // given
    whenever(registerUser.registerUserAtFacility(user)).doReturn(
        Single.just<RegistrationResult>(NetworkError),
        Single.just<RegistrationResult>(Success)
    )

    // when
    setupController()

    // then
    verify(ui).showNetworkError()
    verifyNoMoreInteractions(ui, uiActions)

    // when
    clearInvocations(ui, uiActions)
    uiEvents.onNext(RegisterErrorRetryClicked)

    // then
    verify(uiActions).openHomeScreen()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when screen is created, then the user registration should be attempted`() {
    // given
    whenever(registerUser.registerUserAtFacility(user)) doReturn Single.never()

    // when
    setupController()

    // then
    verify(registerUser).registerUserAtFacility(user)
    verifyZeroInteractions(ui, uiActions)
  }

  @Test
  fun `when the user registration succeeds, then clear registration entry and go to home screen`() {
    // given
    whenever(registerUser.registerUserAtFacility(user)) doReturn Single.just<RegistrationResult>(Success)

    // when
    setupController()

    // then
    verify(uiActions).openHomeScreen()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the user registration fails with a network error, show the network error message`() {
    // given
    whenever(registerUser.registerUserAtFacility(user)) doReturn Single.just<RegistrationResult>(NetworkError)

    // when
    setupController()

    // then
    verify(ui).showNetworkError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the user registration fails with any other error, show the generic error message`() {
    // given
    whenever(registerUser.registerUserAtFacility(user)) doReturn Single.just<RegistrationResult>(UnexpectedError)

    // when
    setupController()

    // then
    verify(ui).showUnexpectedError()
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    val effectHandler = RegistrationLoadingEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        registerUser = registerUser,
        clock = clock,
        passwordHasher = passwordHasher,
        viewEffectsConsumer = RegistrationLoadingViewEffectHandler(uiActions)::handle
    )
    val uiRenderer = RegistrationLoadingUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationLoadingModel.create(registrationEntry),
        update = RegistrationLoadingUpdate(),
        effectHandler = effectHandler.build(),
        init = RegistrationLoadingInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
