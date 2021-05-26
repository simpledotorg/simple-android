package org.simple.clinic.security.pin.verification

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.OngoingLoginEntryRepository

class OngoingLoginEntryPinVerificationMethodTest {

  private val passwordHasher = JavaHashPasswordHasher()

  private val repository = mock<OngoingLoginEntryRepository>()

  private val pinVerificationMethod = OngoingLoginEntryPinVerificationMethod(repository, passwordHasher)

  @Test
  fun `when the entered password matches the saved digest of the entry, it should mark the password as correct`() {
    // given
    val correctPassword = "1234"
    val entry = TestData.ongoingLoginEntry(
        pinDigest = passwordHasher.hash(correctPassword)
    )
    whenever(repository.entryImmediate()) doReturn entry

    // when
    val result = pinVerificationMethod.verify(correctPassword)

    // then
    assertThat(result).isEqualTo(PinVerificationMethod.VerificationResult.Correct(correctPassword))
  }

  @Test
  fun `when the entered password does not match the saved digest of the entry, it should mark the password as incorrect`() {
    // given
    val correctPassword = "1234"
    val entry = TestData.ongoingLoginEntry(
        pinDigest = passwordHasher.hash(correctPassword)
    )
    whenever(repository.entryImmediate()) doReturn entry

    // when
    val enteredPassword = "1111"
    val result = pinVerificationMethod.verify(enteredPassword)

    // then
    assertThat(result).isEqualTo(PinVerificationMethod.VerificationResult.Incorrect(enteredPassword))
  }
}
