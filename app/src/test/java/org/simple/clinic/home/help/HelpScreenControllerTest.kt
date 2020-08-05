package org.simple.clinic.home.help

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.help.HelpPullResult
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpScreenTryAgainClicked
import org.simple.clinic.help.HelpSync
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class HelpScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val screen = mock<HelpScreen>()
  private val helpRepository = mock<HelpRepository>()
  private val helpSync = mock<HelpSync>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when a help file is emitted then update the screen`() {
    val content = "Help"

    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(content.toOptional()))

    setupController()

    verify(screen).showHelp(content)
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `screen should be updated whenever the help file changes`() {
    val please = "Please"
    val help = "Help"

    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(
        please.toOptional(),
        help.toOptional()
    ))

    setupController()

    verify(screen).showHelp(please)
    verify(screen).showHelp(help)
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `when the help file does not exist then screen should show no-help view`() {
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))

    setupController()

    verify(screen).showNoHelpAvailable()
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verifyZeroInteractions(helpSync)
  }

  @Test
  fun `when try again is clicked, the loading view must be shown`() {
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showLoadingView()
    verify(screen).showNoHelpAvailable()
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  @Test
  fun `when try again is clicked, help must be synced`() {
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showNoHelpAvailable()
    verify(screen).showLoadingView()
    verifyNoMoreInteractions(screen)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)
  }

  @Test
  fun `when the help sync fails with a network error, the network error message must be shown`() {
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.NetworkError))

    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showNetworkErrorMessage()
    verify(screen).showLoadingView()
    verify(screen, times(2)).showNoHelpAvailable()
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  @Test
  fun `when the help sync fails with any error except network error, the unexpected error message must be shown`() {
    whenever(helpRepository.helpContentText()).thenReturn(Observable.just(Optional.empty()))
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.OtherError))

    setupController()
    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showUnexpectedErrorMessage()
    verify(screen, times(2)).showNoHelpAvailable()
    verify(screen).showLoadingView()
    verifyNoMoreInteractions(screen)

    verify(helpRepository).helpContentText()
    verifyNoMoreInteractions(helpRepository)

    verify(helpSync).pullWithResult()
    verifyNoMoreInteractions(helpSync)
  }

  private fun setupController() {
    val controller = HelpScreenController(repository = helpRepository, sync = helpSync)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}
