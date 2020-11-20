package org.simple.clinic.registration.register

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class RegistrationLoadingInitTest {

  @Test
  fun `when the screen is created, convert the registration entry to a user for registration`() {
    val facility = TestData.facility(uuid = UUID.fromString("0e1ec3bc-6b54-4587-b6c8-d494b4836b5f"))
    val registrationEntry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("1396a434-fa1c-422a-8096-b7744f8f8408"),
        phoneNumber = "1234567890",
        pin = "1111",
        registrationFacility = facility,
        fullName = "Anish Acharya"
    )
    val model = RegistrationLoadingModel.create(registrationEntry)

    val spec = InitSpec(RegistrationLoadingInit())

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(ConvertRegistrationEntryToUserDetails(registrationEntry))
        ))
  }
}
