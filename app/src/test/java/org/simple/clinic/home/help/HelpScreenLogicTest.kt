package org.simple.clinic.home.help

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.help.HelpPullResult
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpSync
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class HelpScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<HelpScreenUi>()
  private val uiActions = mock<HelpScreenUiActions>()
  private val helpRepository = mock<HelpRepository>()
  private val helpSync = mock<HelpSync>()

  private lateinit var testFixture: MobiusTestFixture<HelpScreenModel, HelpScreenEvent, HelpScreenEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when a help file is emitted then update the screen`() {
    // given
    val content = "Help"

    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(content.toOptional()))

    // when
    setupController()

    // then
    verify(ui).showNoHelpAvailable()
    verify(ui).showHelp(content)
    verifyNoMoreInteractions(ui)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `screen should be updated whenever the help file changes`() {
    // given
    val please = "Please"
    val help = "Help"

    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(
        please.toOptional(),
        help.toOptional()
    ))

    // when
    setupController()

    // then
    verify(ui).showNoHelpAvailable()
    verify(ui).showHelp(please)
    verify(ui).showHelp(help)
    verifyNoMoreInteractions(ui)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `when the help file does not exist then screen should show no-help view`() {
    // given
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))

    // when
    setupController()

    // then
    verify(ui, times(2)).showNoHelpAvailable()
    verifyNoMoreInteractions(ui)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `when try again is clicked, the loading view must be shown`() {
    // given
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    // when
    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    // then
    verify(uiActions).showLoadingView()
    verify(ui, times(2)).showNoHelpAvailable()
    verifyNoMoreInteractions(ui, uiActions)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  @Test
  fun `when try again is clicked, help must be synced`() {
    // given
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    // when
    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    // then
    verify(ui, times(2)).showNoHelpAvailable()
    verify(uiActions).showLoadingView()
    verifyNoMoreInteractions(ui, uiActions)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)
  }

  @Test
  fun `when the help sync fails with a network error, the network error message must be shown`() {
    // given
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.NetworkError))

    // when
    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    // then
    verify(ui).showNetworkErrorMessage()
    verify(uiActions).showLoadingView()
    verify(ui, times(3)).showNoHelpAvailable()
    verifyNoMoreInteractions(ui, uiActions)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  @Test
  fun `when the help sync fails with any error except network error, the unexpected error message must be shown`() {
    // given
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.OtherError))

    // when
    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    // then
    verify(ui).showUnexpectedErrorMessage()
    verify(ui, times(3)).showNoHelpAvailable()
    verify(uiActions).showLoadingView()
    verifyNoMoreInteractions(ui, uiActions)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  private fun setupController() {
    val effectHandler = HelpScreenEffectHandler(
        helpRepository = helpRepository,
        helpSync = helpSync,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )
    val uiRenderer = HelpScreenUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = HelpScreenModel.create(),
        init = HelpScreenInit(),
        update = HelpScreenUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
