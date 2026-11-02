package org.simple.clinic.scanid.scannedqrcode

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId

class ScannedQrCodeUiRendererTest {

  private val ui = mock<ScannedQrCodeUi>()
  private val uiRenderer = ScannedQrCodeUiRenderer(ui)

  @Test
  fun `when the qr code is scanned by a bp passport number, render bp passport values in scanned qr code sheet`() {
    // given
    val defaultModel = ScannedQrCodeModel.create(Identifier(
        value = "2bd05cc3-5c16-464d-87e1-25b6b1a8a99a",
        type = BpPassport
    ))

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showBpPassportValue()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the qr code is scanned by a shared health record id, render shared health record id values in scanned qr code sheet`() {
    // given
    val defaultModel = ScannedQrCodeModel.create(Identifier(
        value = "98146050888",
        type = BangladeshSharedHealthRecordId
    ))

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showBangladeshSharedHealthRecordId()
    verifyNoMoreInteractions(ui)
  }
}
