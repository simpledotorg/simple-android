package org.simple.clinic.scanid

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class ScanSimpleIdScreenControllerTest {

  val uiEvents = PublishSubject.create<UiEvent>()
  val screen = mock<ScanSimpleIdScreen>()
  val controller = ScanSimpleIdScreenController()

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  @Parameters(value = ["scan 1", "scan 2", "986e853b-05c3-482e-946a-9a8d1d5c95c5"])
  fun `when qr code is scanned, the search results screen must be opened`(text: String) {
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(text))

    verify(screen).showPatientSearchResults(text)
  }
}
