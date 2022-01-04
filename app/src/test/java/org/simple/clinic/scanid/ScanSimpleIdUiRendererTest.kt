package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class ScanSimpleIdUiRendererTest {

  private val ui = mock<ScanSimpleIdUi>()
  private val uiRenderer = ScanSimpleIdUiRenderer(ui)

  @Test
  fun `when searching for a patient, then show search progress`() {
    // given
    val searchingModel = ScanSimpleIdModel.create(OpenedFrom.PatientsTabScreen)
        .searching()

    // when
    uiRenderer.render(searchingModel)

    // then
    verify(ui).showSearchingForPatient()
    verify(ui).hideScanError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when searching for patient is completed, then hide search progress`() {
    // given
    val notSearchingModel = ScanSimpleIdModel.create(OpenedFrom.PatientsTabScreen)
        .notSearching()

    // when
    uiRenderer.render(notSearchingModel)

    // then
    verify(ui).hideSearchingForPatient()
    verify(ui).hideScanError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is no scan error, then hide scan error`() {
    // given
    val defaultModel = ScanSimpleIdModel.create(OpenedFrom.PatientsTabScreen)

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hideSearchingForPatient()
    verify(ui).hideScanError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is scan error, then show scan error`() {
    // given
    val defaultModel = ScanSimpleIdModel.create(OpenedFrom.PatientsTabScreen)
        .invalidQrCode()

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hideSearchingForPatient()
    verify(ui).showScanError()
    verifyNoMoreInteractions(ui)
  }
}
