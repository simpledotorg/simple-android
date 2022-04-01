package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import java.time.LocalDate

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
}
