package org.resolvetosavelives.red.qrscan

import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.resolvetosavelives.red.util.RuntimePermissionResult
import org.resolvetosavelives.red.widgets.UiEvent

class AadhaarScanScreenControllerTest {

  private val screen: AadhaarScanScreen = mock()
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private val controller: AadhaarScanScreenController = AadhaarScanScreenController(mock(), mock(), mock())

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when aadhaar scan is clicked but camera permission isn't granted then request for it and enable scan once it's is granted`() {
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.DENIED))
    uiEvents.onNext(AadhaarScanClicked())
    Mockito.verify(screen).requestCameraPermission()
  }

  @Test
  fun `when aadhaar scan is clicked but camera permission has been permanently denied then open app info`() {
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.NEVER_ASK_AGAIN))
    uiEvents.onNext(AadhaarScanClicked())

    Mockito.verify(screen).openAppInfoToManuallyEnableCameraAccess()
  }

  @Test
  fun `toggle Aadhaar scanner with camera permission toggles`() {
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.GRANTED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.DENIED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.GRANTED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.NEVER_ASK_AGAIN))

    Mockito.verify(screen, Mockito.times(2)).setAadhaarScannerEnabled(true)
    Mockito.verify(screen, Mockito.times(2)).setAadhaarScannerEnabled(false)
  }
}
