package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import java.time.LocalDate
import java.util.Optional

class PatientsTabUiRendererTest {
  private val ui = mock<PatientsTabUi>()
  private val uiRenderer = PatientsTabUiRenderer(ui, LocalDate.of(2022, 3, 28))
  private val defaultModel = PatientsTabModel.create(isRemindingToEnterDrugStockReportEnabled = false)

  @Test
  fun `When app staleness is loaded and app update nudge priority is medium, then show critical app update card and display app update nudge reason`() {
    //given
    val appStaleness = 75

    // when
    uiRenderer.render(defaultModel.updateAppStaleness(appStaleness).appUpdateNudgePriorityUpdated(MEDIUM))

    // then
    verify(ui).showCriticalAppUpdateCard()
    verify(ui).renderAppUpdateReason(appStalenessInMonths = 2)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the previous months drug stock report is not filled, then show the drug stock reminder card`() {
    // given
    val model = PatientsTabModel
        .create(isRemindingToEnterDrugStockReportEnabled = true)
        .updateIsDrugStockFilled(Optional.of(false))

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDrugStockReminderCard()
    verifyNoMoreInteractions(ui)
  }
}
