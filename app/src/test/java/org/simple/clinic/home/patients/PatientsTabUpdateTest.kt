package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.analytics.NetworkConnectivityStatus.INACTIVE
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.drugstockreminders.DrugStockReminder
import org.simple.sharedTestCode.TestData
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class PatientsTabUpdateTest {
  private val defaultModel = PatientsTabModel.create()
  private val updateSpec = UpdateSpec(PatientsTabUpdate(isNotifyAppUpdateAvailableV2Enabled = false))

  @Test
  fun `when update now button is clicked, then open Simple on play store`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UpdateNowButtonClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenSimpleOnPlayStore)
            )
        )
  }

  @Test
  fun `when app update is available, update dialog was last shown yesterday and feature flag is disabled, then show app update dialog and touch app update shown time preference`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingAppUpdateLoaded(isAppUpdateAvailable = true,
            appUpdateNudgePriority = null,
            appUpdateLastShownOn = LocalDate.of(2022, 3, 30),
            currentDate = LocalDate.of(2022, 3, 31),
            appStaleness = null
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAppUpdateAvailable, TouchAppUpdateShownAtTime)
        ))
  }

  @Test
  fun `when app update is available, update dialog was last shown today and feature flag is disabled, then do nothing`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingAppUpdateLoaded(isAppUpdateAvailable = true,
            appUpdateNudgePriority = null,
            appUpdateLastShownOn = LocalDate.of(2022, 3, 31),
            currentDate = LocalDate.of(2022, 3, 31),
            appStaleness = null
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when app update nudge priority is light and feature flag is enabled, then show app update dialog, touch app update shown time preference and update the model`() {
    val updateSpec = UpdateSpec(PatientsTabUpdate(isNotifyAppUpdateAvailableV2Enabled = true))
    val appUpdateNudgePriority = LIGHT

    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingAppUpdateLoaded(isAppUpdateAvailable = true,
            appUpdateNudgePriority = appUpdateNudgePriority,
            appUpdateLastShownOn = LocalDate.of(2022, 3, 30),
            currentDate = LocalDate.of(2022, 3, 31),
            appStaleness = 130
        ))
        .then(assertThatNext(
            hasModel(defaultModel.appUpdateNudgePriorityUpdated(appUpdateNudgePriority).updateAppStaleness(130)),
            hasEffects(ShowAppUpdateAvailable, TouchAppUpdateShownAtTime)
        ))
  }

  @Test
  fun `when app update nudge priority is critical and feature flag is enabled, then show critical app update dialog and update the model`() {
    val updateSpec = UpdateSpec(PatientsTabUpdate(isNotifyAppUpdateAvailableV2Enabled = true))
    val appUpdateNudgePriority = CRITICAL

    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingAppUpdateLoaded(isAppUpdateAvailable = true,
            appUpdateNudgePriority = appUpdateNudgePriority,
            appUpdateLastShownOn = LocalDate.of(2022, 3, 30),
            currentDate = LocalDate.of(2022, 3, 31),
            appStaleness = null
        ))
        .then(assertThatNext(
            hasModel(defaultModel.appUpdateNudgePriorityUpdated(appUpdateNudgePriority)),
            hasEffects(ShowCriticalAppUpdateDialog(appUpdateNudgePriority))
        ))
  }

  @Test
  fun `when required info for drug stock report is loaded and it is last checked before today, then load drug stock report status`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingDrugStockReminderLoaded(
            currentDate = LocalDate.parse("2018-02-10"),
            drugStockReportLastCheckedAt = LocalDate.parse("2018-02-09"),
            isDrugStockReportFilled = Optional.of(true)
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadDrugStockReportStatus("2018-01-01"))
        ))
  }

  @Test
  fun `when required info for drug stock report is loaded and it is last checked today, then update model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingDrugStockReminderLoaded(
            currentDate = LocalDate.parse("2018-02-10"),
            drugStockReportLastCheckedAt = LocalDate.parse("2018-02-10"),
            isDrugStockReportFilled = Optional.of(true)
        ))
        .then(assertThatNext(
            hasModel(defaultModel.updateIsDrugStockFilled(Optional.of(true))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when drug stock report is loaded, then update drug stock report last checked at and filled status, and update model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DrugStockReportLoaded(
            result = DrugStockReminder.Result.NotFound
        ))
        .then(assertThatNext(
            hasModel(
                defaultModel.updateIsDrugStockFilled(Optional.of(false))
            ),
            hasEffects(
                TouchDrugStockReportLastCheckedAt,
                TouchIsDrugStockReportFilled(isDrugStockReportFilled = false)
            )
        ))
  }

  @Test
  fun `when enter drug stock button is clicked and the network is not connected, then show no internet connection dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(EnterDrugStockButtonClicked(networkStatus = Optional.of(INACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoActiveNetworkConnectionDialog)
        ))
  }

  @Test
  fun `when enter drug stock button is clicked and the network is connected, then enter drug stock screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(EnterDrugStockButtonClicked(networkStatus = Optional.of(ACTIVE)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenEnterDrugStockScreen)
        ))
  }

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("325b17b1-8cc9-4ee6-9e44-6793bcdccb5f")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasNoEffects()
        ))
  }
}
