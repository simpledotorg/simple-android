package org.simple.clinic.security.pin.verification

import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.UserSession
import java.util.UUID

class LocalUserPinVerificationMethodTest {

  private val passwordHasher = JavaHashPasswordHasher()

  private val userSession = mock<UserSession>()

  private val pinVerificationMethod = LocalUserPinVerificationMethod(userSession, passwordHasher)

  @Test
  fun `when the entered password matches the saved digest of the user, it should mark the password as correct`() {
    // given
    val correctPassword = "1234"
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("3be29dea-b1ca-4113-94c6-db51d4a86f9b"),
        pinDigest = passwordHasher.hash(correctPassword)
    )
    whenever(userSession.loggedInUserImmediate()) doReturn user

    // when
    val result = pinVerificationMethod.verify(correctPassword)

    // then
    assertThat(result).isEqualTo(PinVerificationMethod.VerificationResult.Correct(correctPassword))
  }

  @Test
  fun `when the entered password does not match the saved digest of the user, it should mark the password as incorrect`() {
    // given
    val correctPassword = "1234"
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("3be29dea-b1ca-4113-94c6-db51d4a86f9b"),
        pinDigest = passwordHasher.hash(correctPassword)
    )
    whenever(userSession.loggedInUserImmediate()) doReturn user

    // when
    val enteredPassword = "1111"
    val result = pinVerificationMethod.verify(enteredPassword)

    // then
    assertThat(result).isEqualTo(PinVerificationMethod.VerificationResult.Incorrect(enteredPassword))
  }
}
