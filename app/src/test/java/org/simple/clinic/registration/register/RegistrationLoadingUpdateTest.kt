package org.simple.clinic.registration.register

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.UUID

class RegistrationLoadingUpdateTest {

  @Test
  fun `when we are ready to register the user, do the user registration`() {
    val passwordHasher = JavaHashPasswordHasher()
    val entry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("08ee7f1a-a198-4e2d-8b16-f59bef2482bf"),
        phoneNumber = "1111111111",
        fullName = "Anish Acharya",
        pin = "1111",
        registrationFacility = TestData.facility(uuid = UUID.fromString("54663543-e19c-4248-b0f3-6070576db0cd"))
    )

    val model = RegistrationLoadingModel.create(entry)

    val user = User(
        uuid = entry.uuid!!,
        phoneNumber = entry.phoneNumber!!,
        fullName = entry.fullName!!,
        pinDigest = passwordHasher.hash(entry.pin!!),
        status = UserStatus.WaitingForApproval,
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        registrationFacilityUuid = entry.facilityId!!,
        currentFacilityUuid = entry.facilityId!!,
        teleconsultPhoneNumber = null,
        capabilities = null
    )

    val spec = UpdateSpec(RegistrationLoadingUpdate())

    spec
        .given(model)
        .whenEvent(ConvertedRegistrationEntryToUserDetails(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RegisterUserAtFacility(user))
        ))
  }
}
