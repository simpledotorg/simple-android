package org.simple.clinic.patient.shortcode

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class UuidShortCodeCreatorTest {

  @get:Rule
  val expectedException = ExpectedException.none()

  @Test
  @Parameters(method = "params for generating short codes")
  fun `generating a short code to a given length should work as expected`(
      uuid: UUID,
      shortCodeLength: Int,
      characterFilter: UuidShortCodeCreator.CharacterFilter,
      expectedShortCode: UuidShortCode
  ) {
    val uuidShortCodeCreator = UuidShortCodeCreator(requiredShortCodeLength = shortCodeLength, characterFilter = characterFilter)

    val shortCode = uuidShortCodeCreator.createFromUuid(uuid = uuid)
    assertThat(shortCode).isEqualTo(expectedShortCode)
  }

  @Suppress("Unused")
  private fun `params for generating short codes`(): List<List<Any>> {
    fun testCase(
        uuid: UUID,
        shortCodeLength: Int,
        expectedShortCode: UuidShortCode,
        isCharacterAllowed: (Char) -> Boolean
    ): List<Any> {
      val characterFilter = object : UuidShortCodeCreator.CharacterFilter {
        override fun filter(char: Char) = isCharacterAllowed(char)
      }

      return listOf(uuid, shortCodeLength, characterFilter, expectedShortCode)
    }

    val uuid = UUID.fromString("bc301458-a39b-48d3-8666-9f39373e2d0d")
    return listOf(
        testCase(
            uuid = uuid,
            shortCodeLength = 4,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "bc30"),
            isCharacterAllowed = { it in setOf('b', 'c', '3', '0') }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 6,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "bc303b"),
            isCharacterAllowed = { it in setOf('b', 'c', '3', '0') }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 6,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "301458"),
            isCharacterAllowed = { it.isDigit() }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 8,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "30145839"),
            isCharacterAllowed = { it.isDigit() }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 7,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "bcabdfe"),
            isCharacterAllowed = { it.isLetter() }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 10,
            expectedShortCode = UuidShortCode.CompleteShortCode(uuid, "bc301458a3"),
            isCharacterAllowed = { it.isLetterOrDigit() }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 3,
            expectedShortCode = UuidShortCode.IncompleteShortCode(uuid, "bb", 3),
            isCharacterAllowed = { it in setOf('b') }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 5,
            expectedShortCode = UuidShortCode.IncompleteShortCode(uuid, "bb2", 5),
            isCharacterAllowed = { it in setOf('b', '2') }
        ),
        testCase(
            uuid = uuid,
            shortCodeLength = 9,
            expectedShortCode = UuidShortCode.IncompleteShortCode(uuid, "333333e", 9),
            isCharacterAllowed = { it in setOf('3', 'e') }
        )
    )
  }

  @Test
  @Parameters(value = ["0", "-1", "-10"])
  fun `instantiating the short code creator with a required length less than 1 should fail`(length: Int) {
    expectedException.expect(IllegalArgumentException::class.java)
    expectedException.expectMessage("Short code length must be > 0!")

    UuidShortCodeCreator(
        requiredShortCodeLength = length,
        characterFilter = object : UuidShortCodeCreator.CharacterFilter {
          override fun filter(char: Char) = true
        }
    )
  }
}
