package org.simple.clinic.home.patients

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.User
import org.simple.clinic.util.ValueChangedCallback

class PatientsUiRenderer(private val ui: PatientsUi) : ViewRenderer<PatientsModel> {

  private val userChangedCallback = ValueChangedCallback<User>()

  override fun render(model: PatientsModel) {
    if (model.hasLoadedUser) {
      userChangedCallback.pass(model.user!!) { user ->
        renderSyncIndicatorVisibility(user)
      }
    }
  }

  private fun renderSyncIndicatorVisibility(user: User) {
    if (user.canSyncData) {
      ui.showSyncIndicator()
    } else {
      ui.hideSyncIndicator()
    }
  }
}
