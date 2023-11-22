package org.simple.clinic.facility.alertchange

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation

class AlertFacilityChangeUpdateTest {

  @Test
  fun `when facility changed status is loaded and facility is changed, then update model`() {
    val model = AlertFacilityChangeModel.default()

    UpdateSpec(AlertFacilityChangeUpdate())
        .given(model)
        .whenEvent(IsFacilityChangedStatusLoaded(isFacilityChanged = true))
        .then(assertThatNext(
            hasModel(model.copy(isFacilityChanged = true)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when facility changed status is loaded and facility is not changed, then update model and close sheet`() {
    val model = AlertFacilityChangeModel.default()

    UpdateSpec(AlertFacilityChangeUpdate())
        .given(model)
        .whenEvent(IsFacilityChangedStatusLoaded(isFacilityChanged = false))
        .then(assertThatNext(
            hasModel(model.copy(isFacilityChanged = false)),
            hasEffects(CloseSheetWithContinuation)
        ))
  }
}
