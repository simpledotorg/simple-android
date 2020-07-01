package org.simple.clinic.registration.location

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class RegistrationLocationPermissionUpdateTest {

  @Test
  fun `when the skip button is clicked, the select facility screen must be opened`() {
    val model = RegistrationLocationPermissionModel.create()

    val spec = UpdateSpec(RegistrationLocationPermissionUpdate())

    spec
        .given(model)
        .whenEvent(SkipClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenFacilitySelectionScreen as RegistrationLocationPermissionEffect)
        ))
  }
}
