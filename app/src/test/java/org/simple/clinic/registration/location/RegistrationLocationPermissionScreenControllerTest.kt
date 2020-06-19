package org.simple.clinic.registration.location

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class RegistrationLocationPermissionScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val ui = mock<RegistrationLocationPermissionUi>()

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<RegistrationLocationPermissionModel, RegistrationLocationPermissionEvent, RegistrationLocationPermissionEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when location permission is received then facility selection screen should be opened`() {
    // when
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Optional.of(GRANTED)))

    // then
    verify(ui).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when location permission is denied then facility selection screen should not be opened`() {
    // when
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Optional.of(DENIED)))

    // then
    verify(ui, never()).openFacilitySelectionScreen()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    val controller = RegistrationLocationPermissionScreenController()

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    val uiRenderer = RegistrationLocationPermissionUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationLocationPermissionModel.create(),
        update = RegistrationLocationPermissionUpdate(),
        effectHandler = RegistrationLocationPermissionEffectHandler(ui).build(),
        init = RegistrationLocationPermissionInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
