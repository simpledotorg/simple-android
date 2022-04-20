package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.drugstockreminders.DrugStockReminder
import java.time.LocalDate
import java.util.Optional

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
  fun `when app staleness is loaded, then update the model`() {
    val appStaleness = 75

    updateSpec
        .given(defaultModel)
        .whenEvent(AppStalenessLoaded(appStaleness))
        .then(
            assertThatNext(
                hasModel(defaultModel.updateAppStaleness(appStaleness)),
                hasNoEffects()
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
            currentDate = LocalDate.of(2022, 3, 31)
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
            currentDate = LocalDate.of(2022, 3, 31)
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
            currentDate = LocalDate.of(2022, 3, 31)
        ))
        .then(assertThatNext(
            hasModel(defaultModel.appUpdateNudgePriorityUpdated(appUpdateNudgePriority)),
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
            currentDate = LocalDate.of(2022, 3, 31)
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
  fun `when required info for drug stock report is loaded and it is last checked today, then do nothing`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RequiredInfoForShowingDrugStockReminderLoaded(
            currentDate = LocalDate.parse("2018-02-10"),
            drugStockReportLastCheckedAt = LocalDate.parse("2018-02-10"),
            isDrugStockReportFilled = Optional.of(true)
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when drug stock report is loaded, then update drug stock report last checked at and filled status`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DrugStockReportLoaded(
            result = DrugStockReminder.Result.NotFound
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                TouchDrugStockReportLastCheckedAt,
                TouchIsDrugStockReportFilled(isDrugStockReportFilled = false)
            )
        ))
  }
}
