package org.simple.clinic.registration.facility

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class RegistrationFacilitySelectionUpdateTest {

  @Test
  fun `when show intro video screen is enabled and registration facility is confirmed, then move to intro video screen`() {
    val facilityUuid = UUID.fromString("2f07695d-2fa8-4a6e-bb19-6c8cdb4b9167")
    val entry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("1bd4a093-0ac7-469d-a8a4-6214d5d677bc"),
        registrationFacility = null,
        fullName = "Praveen Mehta",
        phoneNumber = "1111111111",
        pin = "1111"
    )
    val defaultModel = RegistrationFacilitySelectionModel.create(
        entry = entry
    )

    val entryWithFacilityId = entry.withFacilityUuid(facilityUuid)

    UpdateSpec(RegistrationFacilitySelectionUpdate(showIntroVideoScreen = true))
        .given(defaultModel)
        .whenEvent(RegistrationFacilityConfirmed(facilityUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MoveToIntroVideoScreen(entryWithFacilityId))
        ))
  }

  @Test
  fun `when show intro video screen is disabled and registration facility is confirmed, then move to registration loading screen`() {
    val facilityUuid = UUID.fromString("4cefe000-cd0e-42b9-ba84-917543692ad3")
    val entry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("3828bc4f-c4d8-4b32-a830-902d83a52e45"),
        registrationFacility = null,
        fullName = "Praveen Mehta",
        phoneNumber = "1111111111",
        pin = "1111"
    )
    val defaultModel = RegistrationFacilitySelectionModel.create(
        entry = entry
    )

    val entryWithFacilityId = entry.withFacilityUuid(facilityUuid)

    UpdateSpec(RegistrationFacilitySelectionUpdate(showIntroVideoScreen = false))
        .given(defaultModel)
        .whenEvent(RegistrationFacilityConfirmed(facilityUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MoveToRegistrationLoadingScreen(entryWithFacilityId))
        ))
  }
}
