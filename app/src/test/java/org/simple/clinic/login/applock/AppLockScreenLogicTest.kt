package org.simple.clinic.login.applock

import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Instant
import java.util.Optional
import java.util.UUID

class AppLockScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<AppLockScreenUi>()
  private val uiActions = mock<AppLockUiActions>()
  private val lockAfterTimestampValue = MemoryValue(Optional.empty<Instant>())

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("cdb08a78-7bae-44f4-9bb9-40257be58aa4"),
      pinDigest = "actual-hash"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("33459993-53d0-4484-b8a9-66c8b065f07d"),
      name = "PHC Obvious"
  )

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var testFixture: MobiusTestFixture<AppLockModel, AppLockEvent, AppLockEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `On start, the logged in user's full name should be shown`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("6dcb2c31-569e-4911-a378-046faa5fa9ff"),
        name = "PHC Obvious"
    )

    // when
    setupController()

    // then
    verify(ui, times(2)).setUserFullName(loggedInUser.fullName)
    verify(ui).setFacilityName(facility.name)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `On start, the currently selected facility should be shown`() {
    // when
    setupController()

    // then
    verify(ui, times(2)).setUserFullName(loggedInUser.fullName)
    verify(ui).setFacilityName(facility.name)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when forgot pin is clicked then the confirm forgot pin alert must be shown`() {
    // when
    setupController()
    uiEvents.onNext(AppLockForgotPinClicked)

    // then
    verify(ui, times(2)).setUserFullName(loggedInUser.fullName)
    verify(ui).setFacilityName(facility.name)
    verify(uiActions).showConfirmResetPinDialog()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = AppLockEffectHandler(
        currentUser = { loggedInUser },
        currentFacility = { facility },
        hasUserConsentedToDataProtectionPreference = mock(),
        schedulersProvider = TestSchedulersProvider.trampoline(),
        lockAfterTimestampValue = lockAfterTimestampValue,
        viewEffectsConsumer = AppLockViewEffectHandler(uiActions)::handle
    )

    val uiRenderer = AppLockUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = AppLockModel.create(),
        init = AppLockInit(),
        update = AppLockUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
