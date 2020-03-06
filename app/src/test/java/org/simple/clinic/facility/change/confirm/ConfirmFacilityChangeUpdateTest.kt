package org.simple.clinic.facility.change.confirm

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class ConfirmFacilityChangeUpdateTest {

  @Test
  fun `when facility change is confirmed then user's facility should be changed`() {
    val updateSpec = UpdateSpec<ConfirmFacilityChangeModel, ConfirmFacilityChangeEvent, ConfirmFacilityChangeEffect>(ConfirmFacilityChangeUpdate())
    updateSpec
        .given(ConfirmFacilityChangeModel())
        .whenEvent(FacilityChangeConfirmed)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ChangeFacilityEffect as ConfirmFacilityChangeEffect)
            )
        )
  }
}
