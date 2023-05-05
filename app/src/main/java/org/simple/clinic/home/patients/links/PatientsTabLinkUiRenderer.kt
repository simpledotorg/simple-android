package org.simple.clinic.home.patients.links

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.mobius.ViewRenderer

class PatientsTabLinkUiRenderer @AssistedInject constructor(
    @Assisted private val ui: PatientsTabLinkUi,
    @Assisted private val isPatientLineListEnabled: Boolean
) : ViewRenderer<PatientsTabLinkModel> {

  @AssistedFactory
  interface Factory {
    fun create(
        ui: PatientsTabLinkUi,
        isPatientLineListEnabled: Boolean
    ): PatientsTabLinkUiRenderer
  }

  override fun render(model: PatientsTabLinkModel) {
    ui.showOrHidePatientLineListDownload(isPatientLineListEnabled)
    ui.showOrHideMonthlyScreeningReportsView(model.showMonthlyScreeningLink)
    ui.showOrHideMonthlySuppliesReportsView(model.showMonthlySuppliesLink)
    ui.showOrHideLinkView(
        isPatientLineListEnabled ||
            model.showMonthlyScreeningLink ||
            model.showMonthlySuppliesLink
    )
  }
}
