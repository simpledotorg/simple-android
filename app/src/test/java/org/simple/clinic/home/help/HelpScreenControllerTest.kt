package org.simple.clinic.home.help

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.help.HelpPullResult
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpScreenTryAgainClicked
import org.simple.clinic.help.HelpSync
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.io.File
import java.net.URI

class HelpScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val uiEvents = PublishSubject.create<UiEvent>()
  val screen = mock<HelpScreen>()
  val helpRepository = mock<HelpRepository>()
  val helpSync = mock<HelpSync>()

  val controller = HelpScreenController(repository = helpRepository, sync = helpSync)

  @Before
  fun setUp() {
    uiEvents.compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when a help file is emitted then update the screen`() {
    val file: File = mock()
    val uri = URI("")

    whenever(file.toURI()).thenReturn(uri)
    whenever(helpRepository.helpFile()).thenReturn(Observable.just(file.toOptional()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showHelp(uri)
  }

  @Test
  fun `screen should be updated whenever the help file changes`() {
    val file1: File = mock()
    val file2: File = mock()
    val uri1 = URI("uri1")
    val uri2 = URI("uri2")

    whenever(file1.toURI()).thenReturn(uri1)
    whenever(file2.toURI()).thenReturn(uri2)
    whenever(helpRepository.helpFile()).thenReturn(Observable.just(
        file1.toOptional(),
        file2.toOptional()
    ))

    uiEvents.onNext(ScreenCreated())

    val inorder = inOrder(screen)
    inorder.verify(screen).showHelp(uri1)
    inorder.verify(screen).showHelp(uri2)
  }

  @Test
  fun `when the help file does not exist then screen should show no-help view`() {
    whenever(helpRepository.helpFile()).thenReturn(Observable.just(None))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showNoHelpAvailable()
  }

  @Test
  fun `when try again is clicked, the loading view must be shown`() {
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showLoadingView(isVisible = true)
  }

  @Test
  fun `when try again is clicked, help must be synced`() {
    whenever(helpSync.pullWithResult()).thenReturn(Single.never())

    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(helpSync).pullWithResult()
  }

  @Test
  fun `when the help sync fails with a network error, the network error message must be shown`() {
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.NetworkError))

    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showNetworkErrorMessage()
  }

  @Test
  fun `when the help sync fails with any error except network error, the unexpected error message must be shown`() {
    whenever(helpSync.pullWithResult()).thenReturn(Single.just(HelpPullResult.OtherError))

    uiEvents.onNext(HelpScreenTryAgainClicked)

    verify(screen).showUnexpectedErrorMessage()
  }
}
