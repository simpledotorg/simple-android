package org.simple.clinic.registration.location

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.spotify.mobius.Init
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.mobius.first
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class RegistrationLocationPermissionScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val uiEvents = PublishSubject.create<UiEvent>()
  val uiActions = mock<UiActions>()

  private lateinit var controllerSubscription: Disposable
  private lateinit var textFixture: MobiusTestFixture<RegistrationLocationPermissionModel, RegistrationLocationPermissionEvent, RegistrationLocationPermissionEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    textFixture.dispose()
  }

  @Test
  fun `when location permission is received then facility selection screen should be opened`() {
    // when
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Optional.of(GRANTED)))

    // then
    verify(uiActions).openFacilitySelectionScreen()
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    val controller = RegistrationLocationPermissionScreenController()

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(uiActions) }

    textFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationLocationPermissionModel.create(),
        init = Init { first(it) },
        update = RegistrationLocationPermissionUpdate(),
        effectHandler = RegistrationLocationPermissionEffectHandler(
            schedulersProvider = TestSchedulersProvider.trampoline(),
            uiActions = uiActions
        ).build(),
        modelUpdateListener = { /* no-op */ }
    )

    textFixture.start()
  }
}
