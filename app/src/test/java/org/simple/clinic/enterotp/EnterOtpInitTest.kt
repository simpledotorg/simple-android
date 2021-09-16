package org.simple.clinic.enterotp

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class EnterOtpInitTest {

  @Test
  fun `when screen is created, then load the otp entry states`() {
    val initSpec = InitSpec(EnterOtpInit())
    val defaultModel = EnterOtpModel.create(minOtpRetries = 3, maxOtpEntriesAllowed = 5)
    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(LoadOtpEntryProtectedStates)
            )
        )
  }
}
