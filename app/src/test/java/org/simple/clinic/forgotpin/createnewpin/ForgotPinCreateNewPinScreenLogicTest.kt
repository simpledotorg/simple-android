package org.simple.clinic.forgotpin.createnewpin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import dagger.Lazy
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ForgotPinCreateNewPinScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<ForgotPinCreateNewPinUi>()
  private val uiActions = mock<UiActions>()

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("ecf9c120-5e20-4bf5-bfe1-f83e01bcb487"),
      name = "John Doe"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("7a3f0062-d644-45f8-b421-bd4a80ddd238"),
      name = "PHC Obvious"
  )

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var testFixture: MobiusTestFixture<ForgotPinCreateNewModel, ForgotPinCreateNewEvent, ForgotPinCreateNewEffect>

  @After
  fun tearDown() {
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
    verify(uiActions, times(3)).showInvalidPinError()
    verify(uiActions, times(4)).hideInvalidPinError()
    verify(uiActions).showConfirmPinScreen("1111")
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when a complete PIN is submitted, the confirm PIN screen must be shown`() {
    setupController()

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1111"))
    uiEvents.onNext(ForgotPinCreateNewPinSubmitClicked)

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showConfirmPinScreen("1111")
    verify(uiActions).hideInvalidPinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the PIN text changes, any error must be hidden`() {
    setupController()

    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("1"))
    uiEvents.onNext(ForgotPinCreateNewPinTextChanged("11"))

    verify(ui).showUserName("John Doe")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions, times(2)).hideInvalidPinError()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = ForgotPinCreateNewEffectHandler(
        currentUser = Lazy { loggedInUser },
        currentFacility = Lazy { facility },
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
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
