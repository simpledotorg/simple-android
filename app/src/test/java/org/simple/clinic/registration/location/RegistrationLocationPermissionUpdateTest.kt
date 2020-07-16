package org.simple.clinic.registration.location

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import java.util.UUID

class RegistrationLocationPermissionUpdateTest {

  @Test
  fun `when the skip button is clicked, the select facility screen must be opened`() {
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.fromString("759f5f53-6f71-4a00-825b-c74654a5e448"),
        phoneNumber = "1111111111",
        fullName = "Anish Acharya",
        pin = "1234"
    )

    val model = RegistrationLocationPermissionModel.create(ongoingEntry)

    val spec = UpdateSpec(RegistrationLocationPermissionUpdate())

    spec
        .given(model)
        .whenEvent(SkipClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenFacilitySelectionScreen(ongoingEntry) as RegistrationLocationPermissionEffect)
        ))
  }
}
