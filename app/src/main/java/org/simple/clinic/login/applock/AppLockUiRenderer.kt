package org.simple.clinic.login.applock

import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.User

class AppLockUiRenderer(private val ui: AppLockScreenUi) : ViewRenderer<AppLockModel> {

  override fun render(model: AppLockModel) {
    if (model.hasUser) {
      renderUsername(model.user!!)
    }

    if (model.hasFacility) {
      renderFacilityName(model.facility!!)
    }
  }

  private fun renderUsername(user: User) {
    ui.setUserFullName(user.fullName)
  }

  private fun renderFacilityName(facility: Facility) {
    ui.setFacilityName(facility.name)
  }
}
