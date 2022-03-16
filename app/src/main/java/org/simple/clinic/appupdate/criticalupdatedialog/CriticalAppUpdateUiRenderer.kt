package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.mobius.ViewRenderer
import java.time.LocalDate
import java.time.Period

class CriticalAppUpdateUiRenderer(
    private val ui: CriticalAppUpdateUi
) : ViewRenderer<CriticalAppUpdateModel> {

  override fun render(model: CriticalAppUpdateModel) {
    if (model.hasHelpContact) {
      ui.showHelp()
    } else {
      ui.hideHelp()
    }

    if (model.isCriticalUpdateNudgePriority && model.hasAppStaleness)
      ui.renderCriticalAppUpdateReason(appStalenessInMonths(model.appStaleness!!))

    if (model.isCriticalSecurityUpdateNudgePriority)
      ui.renderCriticalSecurityAppUpdateReason()
  }

  private fun appStalenessInMonths(appStaleness: Int): Int {
    val currentDate = LocalDate.now()
    val lastUpdatedDate = currentDate.minusDays(appStaleness.toLong())
    return Period.between(lastUpdatedDate, currentDate).months
  }
}
