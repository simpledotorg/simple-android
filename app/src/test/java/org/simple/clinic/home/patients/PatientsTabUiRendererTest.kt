package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.sharedTestCode.TestData
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class PatientsTabUiRendererTest {
  private val ui = mock<PatientsTabUi>()
  private val uiRenderer = PatientsTabUiRenderer(
      ui = ui,
      currentDate = LocalDate.of(2022, 3, 28),
      isPatientLineListEnabled = false
  )
  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `When app staleness is loaded and app update nudge priority is medium and, then show critical app update card and display app update nudge reason`() {
    //given
    val appStaleness = 75
    val model = defaultModel.numberOfPatientsRegisteredUpdated(12)

    // when
    uiRenderer.render(model.updateAppStaleness(appStaleness).appUpdateNudgePriorityUpdated(MEDIUM))

    // then
    verify(ui).showCriticalAppUpdateCard()
    verify(ui).renderAppUpdateReason(appStalenessInMonths = 2)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the previous months drug stock report is not filled, then show the drug stock reminder card`() {
    // given
    val model = defaultModel
        .updateIsDrugStockFilled(Optional.of(false))

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDrugStockReminderCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug stock reminder clashes with app update reminder, then show critical app update reminder card`() {
    // given
    val appStaleness = 75
    val model = defaultModel
        .updateIsDrugStockFilled(Optional.of(false))
        .numberOfPatientsRegisteredUpdated(12)
        .updateAppStaleness(appStaleness)
        .appUpdateNudgePriorityUpdated(MEDIUM)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showCriticalAppUpdateCard()
    verify(ui).renderAppUpdateReason(appStalenessInMonths = 2)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the previous months drug stock report is not filled and registered patients are greater than 10, then show the drug stock reminder card`() {
    // given
    val model = defaultModel
        .updateIsDrugStockFilled(Optional.of(false))
        .numberOfPatientsRegisteredUpdated(12)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDrugStockReminderCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the number of patients registered are greater than 10 and drug stock is filled, then show simple illustration`() {
    // given
    val model = defaultModel
        .updateIsDrugStockFilled(Optional.of(true))
        .numberOfPatientsRegisteredUpdated(12)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showIllustration()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the previous months drug stock report is filled, then hide the drug stock reminder card`() {
    // given
    val model = defaultModel
        .updateIsDrugStockFilled(Optional.of(true))
        .numberOfPatientsRegisteredUpdated(0)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showSimpleVideo()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the previous months drug stock report is null and the number of patients registered are less than 10, then show simple video`() {
    // given
    val model = defaultModel
        .numberOfPatientsRegisteredUpdated(8)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showSimpleVideo()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient line list download is enabled and facility is loaded, then show patient line list download option`() {
    // given
    val uiRenderer = PatientsTabUiRenderer(
        ui = ui,
        currentDate = LocalDate.parse("2018-01-01"),
        isPatientLineListEnabled = true
    )
    val facility = TestData.facility(
        uuid = UUID.fromString("fe8f9b3f-0de1-4f0f-8d8e-89fa061adf9e"),
        name = "PHC Obvious"
    )
    val model = defaultModel
        .currentFacilityLoaded(facility)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showPatientLineListDownload(facilityName = "PHC Obvious")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient line list download is enabled and facility is not loaded, then hide patient line list download option`() {
    // given
    val uiRenderer = PatientsTabUiRenderer(
        ui = ui,
        currentDate = LocalDate.parse("2018-01-01"),
        isPatientLineListEnabled = true
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hidePatientLineListDownload()
    verifyNoMoreInteractions(ui)
  }
}
