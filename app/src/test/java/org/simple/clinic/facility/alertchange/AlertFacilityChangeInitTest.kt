package org.simple.clinic.facility.alertchange

import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import org.junit.Test
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.LoadIsFacilityChangedStatus

class AlertFacilityChangeInitTest {

  @Test
  fun `when screen is created, then load if facility is changed`() {
    val model = AlertFacilityChangeModel.default()

    InitSpec(AlertFacilityChangeInit())
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadIsFacilityChangedStatus)
        ))
  }
}
