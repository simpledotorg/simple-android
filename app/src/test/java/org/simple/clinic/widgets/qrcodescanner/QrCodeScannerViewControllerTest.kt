package org.simple.clinic.widgets.qrcodescanner

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class QrCodeScannerViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val uiEvents = PublishSubject.create<UiEvent>()
  val view = mock<QrCodeScannerView>()

  val controller = QrCodeScannerViewController()

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(view) }
  }

  @Test
  fun `when the screen is created, the camera preview must be started`() {
    uiEvents.onNext(ScreenCreated())

    verify(view).startScanning()
  }

  @Test
  fun `the camera preview must be started and stopped based on the activity events`() {
    uiEvents.onNext(TheActivityLifecycle.Paused())
    uiEvents.onNext(TheActivityLifecycle.Resumed())
    uiEvents.onNext(TheActivityLifecycle.Paused())
    uiEvents.onNext(TheActivityLifecycle.Resumed())

    val inOrder = inOrder(view)
    inOrder.verify(view).stopScanning()
    inOrder.verify(view).startScanning()
    inOrder.verify(view).stopScanning()
    inOrder.verify(view).startScanning()
  }

  @Test
  fun `when the view is detached the camera preview must be stopped`() {
    uiEvents.onNext(ScreenDestroyed())

    verify(view).stopScanning()
  }
}
