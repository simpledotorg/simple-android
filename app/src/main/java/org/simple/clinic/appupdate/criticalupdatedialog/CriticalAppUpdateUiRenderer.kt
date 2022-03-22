package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.mobius.ViewRenderer
import java.time.LocalDate
import java.time.Period

class CriticalAppUpdateUiRenderer(
    private val ui: CriticalAppUpdateUi,
    private val currentDate: LocalDate
) : ViewRenderer<CriticalAppUpdateModel> {

  override fun render(model: CriticalAppUpdateModel) {
    if (model.hasHelpContact) {
      ui.showHelp()
    } else {
      ui.hideHelp()
    }

    renderCriticalAppUpdateReason(model)
  }

  private fun renderCriticalAppUpdateReason(model: CriticalAppUpdateModel) {
    if (model.isCriticalUpdateNudgePriority && model.hasAppStaleness) {
      ui.renderCriticalAppUpdateReason(appStalenessInMonths(model.appStaleness!!))
    } else if (model.isCriticalSecurityUpdateNudgePriority) {
      ui.renderCriticalSecurityAppUpdateReason()
    }
  }

  private fun appStalenessInMonths(appStaleness: Int): Int {
    val lastUpdatedDate = currentDate.minusDays(appStaleness.toLong())
    return Period.between(lastUpdatedDate, currentDate).months
  }
}
