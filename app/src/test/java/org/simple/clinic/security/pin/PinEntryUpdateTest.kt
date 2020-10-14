package org.simple.clinic.security.pin

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.NetworkError
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.OtherError
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.ServerError

class PinEntryUpdateTest {

  private val spec = UpdateSpec(PinEntryUpdate(submitPinAtLength = 4))

  private val defaultModel = PinEntryModel.default()

  @Test
  fun `when the PIN verification fails with a network error, show the network error message`() {
    val model = defaultModel.enteredPinChanged("1234")

    spec
        .given(model)
        .whenEvent(PinVerified(NetworkError))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowNetworkError, AllowPinEntry)
            )
        )
  }

  @Test
  fun `when the PIN verification fails with a server error, show the server error message`() {
    val model = defaultModel.enteredPinChanged("1234")

    spec
        .given(model)
        .whenEvent(PinVerified(ServerError))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowServerError, AllowPinEntry)
            )
        )
  }

  @Test
  fun `when the PIN verification fails with any other error, show the generic error message`() {
    val model = defaultModel.enteredPinChanged("1234")

    spec
        .given(model)
        .whenEvent(PinVerified(OtherError(RuntimeException())))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowUnexpectedError, AllowPinEntry)
            )
        )
  }

  @Test
  fun `when pin entry done is clicked, then do nothing`() {
    spec
        .given(defaultModel)
        .whenEvent(PinEntryDoneClicked)
        .then(assertThatNext(
            hasNothing()
        ))
  }
}
