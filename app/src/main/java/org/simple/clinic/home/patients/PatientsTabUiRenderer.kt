package org.simple.clinic.home.patients

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.User
import org.simple.clinic.util.ValueChangedCallback

class PatientsTabUiRenderer(
    private val ui: PatientsTabUi
) : ViewRenderer<PatientsTabModel> {

  private val userChangedCallback = ValueChangedCallback<User>()

  private val numberOfPatientsRegisteredChangedCallback = ValueChangedCallback<Int>()

  override fun render(model: PatientsTabModel) {
    if (model.hasLoadedUser) {
      toggleSyncIndicatorVisibility(model)

      val currentUser = model.user!!
      when {
        currentUser.isPendingSmsVerification -> {
          ui.showUserStatusAsPendingVerification()
        }
        currentUser.isWaitingForApproval -> {
          // User is waiting for approval (new registration or login on a new device before being approved).
          ui.showUserStatusAsWaitingForApproval()
        }
        else -> ui.hideUserAccountStatus()
      }
    }

    if (model.hasLoadedNumberOfPatientsRegistered) {
      toggleTrainingViewVisibility(model)
    }
  }

  private fun toggleSyncIndicatorVisibility(model: PatientsTabModel) {
    userChangedCallback.pass(model.user!!) { user ->
      renderSyncIndicatorVisibility(user)
    }
  }

  private fun toggleTrainingViewVisibility(model: PatientsTabModel) {
    numberOfPatientsRegisteredChangedCallback.pass(model.numberOfPatientsRegistered!!) { numberOfPatientsRegistered ->
      // TODO (vs) 27/05/20: Move this magic number to the constructor
      if (numberOfPatientsRegistered < 10) {
        ui.showSimpleVideo()
      } else {
        ui.showIllustration()
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
