package org.simple.clinic.facility.change.confirm

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ConfirmFacilityChangeUpdateTest {

  private val updateSpec = UpdateSpec(ConfirmFacilityChangeUpdate())

  private val currentFacility = TestData.facility(UUID.fromString("3d362e26-c8ca-4bd6-8910-4fbcbcb98678"))

  private val defaultModel = ConfirmFacilityChangeModel.create()

  @Test
  fun `when facility change is confirmed then user's facility should be changed`() {
    val selectedFacility = TestData.facility(UUID.fromString("ef47531f-9b8c-4045-8578-eda31f0952c4"))

    updateSpec
        .given(defaultModel)
        .whenEvent(FacilityChangeConfirmed(selectedFacility))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ChangeFacilityEffect(selectedFacility) as ConfirmFacilityChangeEffect)
            )
        )
  }

  @Test
  fun `when user's facility is changed then the sheet should be closed`() {
    val facility = TestData.facility(uuid = UUID.fromString("21707c61-12d4-4bae-bedb-d26367d4afcb"))

    updateSpec
        .given(defaultModel)
        .whenEvent(FacilityChanged(facility))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(CloseSheet as ConfirmFacilityChangeEffect)
            )
        )
  }

  @Test
  fun `when the current facility is loaded, then update the ui`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(currentFacility))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(currentFacility)),
                hasNoEffects()
            )
        )
  }
}
