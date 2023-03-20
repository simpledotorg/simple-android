package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import java.time.LocalDate
import java.util.Optional

class PatientsTabUiRendererTest {
  private val ui = mock<PatientsTabUi>()
  private val uiRenderer = PatientsTabUiRenderer(
      ui = ui,
      currentDate = LocalDate.of(2022, 3, 28),
  )
  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `When app staleness is loaded and app update nudge priority is medium and, then show critical app update card and display app update nudge reason`() {
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

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showIllustration()
    verifyZeroInteractions(ui)
  }
}
