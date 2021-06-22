package org.simple.clinic.recentpatient

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class AllRecentPatientsInitTest {

  @Test
  fun `when screen is created, then load all recent patients`() {
    val defaultModel = AllRecentPatientsModel.create()

    InitSpec(AllRecentPatientsInit())
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadAllRecentPatients)
        ))
  }
}
