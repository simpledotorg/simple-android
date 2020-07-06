package org.simple.clinic.registration.location

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.Just
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class RegistrationLocationPermissionScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val uiEvents = PublishSubject.create<UiEvent>()
  val screen = mock<RegistrationLocationPermissionScreen>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when location permission is received then facility selection screen should be opened`() {
    setupController()
    uiEvents.onNext(RequestLocationPermission(permission = Just(GRANTED)))

    verify(screen).openFacilitySelectionScreen()
  }

  private fun setupController() {
    val controller = RegistrationLocationPermissionScreenController()

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
