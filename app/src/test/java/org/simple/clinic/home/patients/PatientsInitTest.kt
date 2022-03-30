package org.simple.clinic.home.patients

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class PatientsInitTest {

  @Test
  fun `when screen is created and feature flag is enabled, then schedule app update notification worker`() {
    val defaultModel = PatientsTabModel.create()

    InitSpec(PatientsInit(isNotifyAppUpdateAvailableV2Enabled = true))
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                ScheduleAppUpdateNotification,
                LoadUser,
                RefreshUserDetails,
                LoadNumberOfPatientsRegistered,
                LoadInfoForShowingAppUpdateMessage
            )
        ))
  }
}
