package org.simple.clinic.home.patients

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class PatientsInitTest {

  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `when screen is created and app update v2 notification feature flag is enabled, then schedule app update notification worker`() {
    val initSpec = InitSpec(PatientsInit(
        isNotifyAppUpdateAvailableV2Enabled = true,
        isMonthlyDrugStockReportReminderEnabledInIndia = false,
        isPatientLineListEnabled = false
    ))

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                ScheduleAppUpdateNotification,
                LoadUser,
                RefreshUserDetails,
                LoadInfoForShowingAppUpdateMessage
            )
        ))
  }

  @Test
  fun `when screen is created and monthly drug stock report reminder feature flag is enabled, then load info for showing drug stock reminder`() {
    val initSpec = InitSpec(PatientsInit(
        isNotifyAppUpdateAvailableV2Enabled = false,
        isMonthlyDrugStockReportReminderEnabledInIndia = true,
        isPatientLineListEnabled = false
    ))

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                LoadUser,
                RefreshUserDetails,
                LoadInfoForShowingAppUpdateMessage,
                LoadInfoForShowingDrugStockReminder
            )
        ))
  }

  @Test
  fun `when screen is created and patient line list feature flag is enabled, then load current facility`() {
    val initSpec = InitSpec(PatientsInit(
        isNotifyAppUpdateAvailableV2Enabled = false,
        isMonthlyDrugStockReportReminderEnabledInIndia = false,
        isPatientLineListEnabled = true
    ))

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                LoadUser,
                RefreshUserDetails,
                LoadInfoForShowingAppUpdateMessage,
                LoadCurrentFacility
            )
        ))
  }

  @Test
  fun `when screen is created and patient line list feature flag is disable, then don't load current facility`() {
    val initSpec = InitSpec(PatientsInit(
        isNotifyAppUpdateAvailableV2Enabled = false,
        isMonthlyDrugStockReportReminderEnabledInIndia = false,
        isPatientLineListEnabled = false
    ))

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                LoadUser,
                RefreshUserDetails,
                LoadInfoForShowingAppUpdateMessage
            )
        ))
  }
}
