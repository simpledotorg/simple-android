package org.simple.clinic.scanid

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

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
  @Parameters(method = "params for scanning simple passport qr code")
  fun `when simple passport qr code is scanned, the parsed id must be sent`(
      scannedText: String,
      expectedScannedCode: UUID
  ) {
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedText))

    verify(screen).sendScannedPassportCode(expectedScannedCode)
  }

  @Suppress("Unused")
  private fun `params for scanning simple passport qr code`(): List<List<Any>> {
    fun testCase(uuidString: String): List<Any> {
      return listOf(uuidString, UUID.fromString(uuidString))
    }

    return listOf(
        testCase("ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
        testCase("83ffb9a1-3b11-4062-80d5-a6deb5f8b927"),
        testCase("d7b0cf0b-8467-4969-8f17-f98f48badb5a")
    )
  }

  @Test
  @Parameters(method = "params for scanning valid uuids")
  fun `the qr code must be sent only if it is a valid uuid`(
      scannedTexts: List<String>,
      expectedScannedCode: UUID?
  ) {
    scannedTexts.forEach { scannedText ->
      uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedText))
    }

    if (expectedScannedCode == null) {
      verify(screen, never()).sendScannedPassportCode(any())
    } else {
      verify(screen).sendScannedPassportCode(expectedScannedCode)
    }
  }

  @Suppress("Unused")
  private fun `params for scanning valid uuids`(): List<List<Any?>> {
    fun testCase(scannedTexts: List<String>, expectedUuid: UUID?): List<Any?> {
      return listOf(scannedTexts, expectedUuid)
    }

    return listOf(
        testCase(emptyList(), null),
        testCase(listOf("a"), null),
        testCase(listOf("a", "b2", "c5123"), null),
        testCase(
            listOf("ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a", "b5"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "d7b0cf0b-8467-4969-8f17-f98f48badb5a", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("d7b0cf0b-8467-4969-8f17-f98f48badb5a"))
    )
  }
}
