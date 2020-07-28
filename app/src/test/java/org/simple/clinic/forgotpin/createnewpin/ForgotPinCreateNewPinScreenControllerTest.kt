package org.simple.clinic.forgotpin.createnewpin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ForgotPinCreateNewPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<ForgotPinCreateNewPinUi>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("ecf9c120-5e20-4bf5-bfe1-f83e01bcb487"),
      name = "John Doe"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("7a3f0062-d644-45f8-b421-bd4a80ddd238"),
      name = "PHC Obvious"
  )

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<ForgotPinCreateNewModel, ForgotPinCreateNewEvent, ForgotPinCreateNewEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `on start, the logged in user's full name must be shown`() {
    setupController()

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    setupController()

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an incomplete PIN is submitted, an error must be shown`() {
    setupController()

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("11"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verify(ui, times(3)).showInvalidPinError()
    verify(ui, times(4)).hideInvalidPinError()
    verify(ui).showConfirmPinScreen("1111")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a complete PIN is submitted, the confirm PIN screen must be shown`() {
    setupController()

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verify(ui).showConfirmPinScreen("1111")
    verify(ui).hideInvalidPinError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the PIN text changes, any error must be hidden`() {
    setupController()

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1"))
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("11"))

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verify(ui, times(2)).hideInvalidPinError()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    val controller = ForgotPinCreateNewPinScreenController(userSession, facilityRepository)

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just((loggedInUser)))
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))

    controllerSubscription = uiEvents.compose(controller)
        .subscribe { it.invoke(ui) }

    uiEvents.onNext(ScreenCreated())

    val effectHandler = ForgotPinCreateNewEffectHandler(
        userSession = userSession,
        facilityRepository = facilityRepository,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = ui
    )
    val uiRenderer = ForgotPinCreateNewUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ForgotPinCreateNewModel.create(),
        init = ForgotPinCreateNewInit(),
        update = ForgotPinCreateNewUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
