package org.simple.clinic.security.pin

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.DEMO_USER_ID
import org.simple.sharedTestCode.TestData
import org.simple.clinic.security.pin.verification.LoginPinServerVerificationMethod
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
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

  @Test
  fun `when demo facility is saved, then record correct pin entered and open home screen`() {
    val userPayload = TestData.loggedInUserPayload(
        uuid = DEMO_USER_ID
    )
    spec
        .given(defaultModel)
        .whenEvent(DemoFacilitySaved(userPayload))
        .then(assertThatNext(
            hasEffects(
                RecordSuccessfulAttempt,
                CorrectPinEntered(userPayload)
            )
        ))
  }

  @Test
  fun `when verified pin is for demo user, then save demo facility`() {
    val loggedInUser = TestData.loggedInUserPayload(
        uuid = DEMO_USER_ID
    )
    val userData = LoginPinServerVerificationMethod.UserData.create(
        pin = "0000",
        payload = loggedInUser
    )

    spec
        .given(defaultModel)
        .whenEvent(PinVerified(Correct(userData)))
        .then(assertThatNext(
            hasEffects(
                SaveDemoFacility(userData)
            )
        ))
  }
}
