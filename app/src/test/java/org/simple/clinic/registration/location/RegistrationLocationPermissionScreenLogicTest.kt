package org.simple.clinic.registration.location

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class RegistrationLocationPermissionScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<RegistrationLocationPermissionUi>()

  private val ongoingEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("759f5f53-6f71-4a00-825b-c74654a5e448"),
      phoneNumber = "1111111111",
      fullName = "Anish Acharya",
      pin = "1234"
  )

  private lateinit var testFixture: MobiusTestFixture<RegistrationLocationPermissionModel, RegistrationLocationPermissionEvent, RegistrationLocationPermissionEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when location permission is received then facility selection screen should be opened`() {
    // when
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Optional.of(GRANTED)))

    // then
    verify(ui).openFacilitySelectionScreen(ongoingEntry)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when location permission is denied then facility selection screen should not be opened`() {
    // when
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Optional.of(DENIED)))

    // then
    verify(ui, never()).openFacilitySelectionScreen(any())
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    val uiRenderer = RegistrationLocationPermissionUiRenderer(ui)
    val effectHandler = RegistrationLocationPermissionEffectHandler(TestSchedulersProvider.trampoline(), ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationLocationPermissionModel.create(ongoingEntry),
        update = RegistrationLocationPermissionUpdate(),
        effectHandler = effectHandler.build(),
        init = RegistrationLocationPermissionInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
