package org.simple.clinic.home.patients

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.User
import org.simple.clinic.util.ValueChangedCallback

class PatientsUiRenderer(private val ui: PatientsUi) : ViewRenderer<PatientsModel> {

  private val userChangedCallback = ValueChangedCallback<User>()

  private val numberOfPatientsRegisteredChangedCallback = ValueChangedCallback<Int>()

  override fun render(model: PatientsModel) {
    if (model.hasLoadedUser) {
      userChangedCallback.pass(model.user!!) { user ->
        renderSyncIndicatorVisibility(user)
      }
    }

    if (model.hasLoadedNumberOfPatientsRegistered) {
      numberOfPatientsRegisteredChangedCallback.pass(model.numberOfPatientsRegistered!!) { numberOfPatientsRegistered ->
        // TODO (vs) 27/05/20: Move this magic number to the constructor
        if (numberOfPatientsRegistered < 10) {
          ui.showSimpleVideo()
        } else {
          ui.showIllustration()
        }
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
