package org.simple.clinic.home.patients.links

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.sharedTestCode.TestData

class PatientsTabLinkUiRendererTest {
  private val ui = mock<PatientsTabLinkUi>()
  private val defaultModel = PatientsTabLinkModel.default()

  @Test
  fun `when monthlyScreeningReportsEnabled is enabled, then show monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        monthlyScreeningReportsEnabled = true
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )


    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(true)
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlyScreeningReportsEnabled is disabled, then hide monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        monthlyScreeningReportsEnabled = false
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )


    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlyScreeningReportsEnabled and patient line list download is disabled, then hide link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        monthlyScreeningReportsEnabled = false
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )


    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient line list download is enabled, then show patient line list download option`() {
    // given
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }
}
