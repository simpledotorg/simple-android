package org.simple.clinic.facility.alertchange

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.MarkFacilityChangedAsFalse
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChanged
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChangedMarkedAsFalse
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.YesButtonClicked
import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation

class AlertFacilityChangeUpdateTest {

  private val defaultModel = AlertFacilityChangeModel.default()
  private val updateSpec = UpdateSpec(AlertFacilityChangeUpdate())

  @Test
  fun `when facility changed status is loaded and facility is changed, then update model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(IsFacilityChangedStatusLoaded(isFacilityChanged = true))
        .then(assertThatNext(
            hasModel(defaultModel.copy(isFacilityChanged = true)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when facility changed status is loaded and facility is not changed, then update model and close sheet`() {
    val model = AlertFacilityChangeModel.default()

    updateSpec
        .given(defaultModel)
        .whenEvent(IsFacilityChangedStatusLoaded(isFacilityChanged = false))
        .then(assertThatNext(
            hasModel(defaultModel.copy(isFacilityChanged = false)),
            hasEffects(CloseSheetWithContinuation)
        ))
  }

  @Test
  fun `when yes button is clicked, then mark facility changed as false`() {
    val model = defaultModel.updateIsFacilityChanged(isFacilityChanged = true)

    updateSpec
        .given(model)
        .whenEvent(YesButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkFacilityChangedAsFalse)
        ))
  }

  @Test
  fun `when facility changed status is marked as false, then close sheet`() {
    val model = defaultModel.updateIsFacilityChanged(isFacilityChanged = true)

    updateSpec
        .given(model)
        .whenEvent(FacilityChangedMarkedAsFalse)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheetWithContinuation)
        ))
  }

  @Test
  fun `when facility is changed, then mark facility changed as false`() {
    val model = defaultModel.updateIsFacilityChanged(isFacilityChanged = true)

    updateSpec
        .given(model)
        .whenEvent(FacilityChanged)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkFacilityChangedAsFalse)
        ))
  }
}
