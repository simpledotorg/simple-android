package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.util.UUID

class PatientsTabUpdateTest {

  private val spec = UpdateSpec(PatientsTabUpdate())

  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("89865fa0-f6e1-48e6-b4d3-206584bb708c"),
      status = UserStatus.ApprovedForSyncing,
      loggedInStatus = User.LoggedInStatus.LOGGED_IN
  )

  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `when the patient short code is entered, the short code search screen must be opened`() {
    val model = defaultModel
        .userLoaded(user)
        .numberOfPatientsRegisteredUpdated(0)

    val shortCode = "1234567"

    spec
        .given(model)
        .whenEvent(BusinessIdScanned.ByShortCode(shortCode))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenShortCodeSearchScreen(shortCode))
        ))
  }
}
