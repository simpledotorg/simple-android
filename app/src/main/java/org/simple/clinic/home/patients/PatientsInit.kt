package org.simple.clinic.home.patients

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientsInit(
    private val isNotifyAppUpdateAvailableV2Enabled: Boolean,
    private val isMonthlyDrugStockReportReminderEnabledInIndia: Boolean
) : Init<PatientsTabModel, PatientsTabEffect> {

  override fun init(model: PatientsTabModel): First<PatientsTabModel, PatientsTabEffect> {
    val effects = mutableSetOf(
        LoadUser,
        RefreshUserDetails,
        LoadNumberOfPatientsRegistered,
        LoadInfoForShowingAppUpdateMessage
    )

    if (isNotifyAppUpdateAvailableV2Enabled) {
      effects.add(ScheduleAppUpdateNotification)
    }

    if (isMonthlyDrugStockReportReminderEnabledInIndia) {
      effects.add(LoadInfoForShowingDrugStockReminder)
    }

    return first(model, effects)
  }
}
