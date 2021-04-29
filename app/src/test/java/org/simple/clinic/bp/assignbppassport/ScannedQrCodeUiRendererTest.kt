package org.simple.clinic.bp.assignbppassport

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId

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
  fun `when the qr code is scanned by a national health id number, render national health id values in scanned qr code sheet`() {
    // given
    val defaultModel = ScannedQrCodeModel.create(Identifier(
        value = "28-3123-2283-6682",
        type = IndiaNationalHealthId
    ))

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showIndianNationalHealthIdValue()
    verifyNoMoreInteractions(ui)
  }
}
