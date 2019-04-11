package org.simple.clinic.util.identifierdisplay

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter.IdentifierToTextConverter

@RunWith(JUnitParamsRunner::class)
class IdentifierDisplayAdapterTest {

  @Test
  fun `the adapter should use the provided converter for converting the identifier value`() {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(
        converters = mapOf(
            BpPassport to StubIdentifierToTextConverter(convertValueAction = { "bp_passport_${it.value}" })
        ),
        unknownValueFallback = { throw RuntimeException() }
    )
    val identifiers = listOf("id_1", "id_2", "id_3").map { Identifier(value = it, type = BpPassport) }

    val convertedForDisplay = identifiers.map(identifierDisplayAdapter::valueAsText)

    val expected = listOf("bp_passport_id_1", "bp_passport_id_2", "bp_passport_id_3")

    assertThat(convertedForDisplay).isEqualTo(expected)
  }

  @Test
  @Parameters(method = "params for unknown identifier value fallback")
  fun `values of unknown identifiers should be converted to using the fallback`(
      fallback: (Identifier) -> String,
      unknownIdentifier: Identifier,
      expectedConvertedUnknownValue: String
  ) {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(
        converters = mapOf(
            BpPassport to StubIdentifierToTextConverter(convertValueAction = { "bp_passport_${it.value}" })
        ),
        unknownValueFallback = fallback
    )
    val knownIdentifier = Identifier(value = "id_1", type = BpPassport)

    val convertedForDisplay = listOf(knownIdentifier, unknownIdentifier)
        .map(identifierDisplayAdapter::valueAsText)

    val expected = listOf("bp_passport_id_1", expectedConvertedUnknownValue)
    assertThat(convertedForDisplay).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for unknown identifier value fallback`(): List<List<Any>> {
    fun testCase(
        fallback: (Identifier) -> String,
        unknownIdentifier: Identifier,
        expectedConvertedUnknownValue: String
    ): List<Any> {
      return listOf(fallback, unknownIdentifier, expectedConvertedUnknownValue)
    }

    return listOf(
        testCase(
            fallback = { it.value },
            unknownIdentifier = Identifier(value = "1234567", type = Unknown(actual = "bp_passport_short_code")),
            expectedConvertedUnknownValue = "1234567"
        ),
        testCase(
            fallback = { "" },
            unknownIdentifier = Identifier(value = "1234567", type = Unknown(actual = "bp_passport_short_code")),
            expectedConvertedUnknownValue = ""
        ),
        testCase(
            fallback = { "unknown" },
            unknownIdentifier = Identifier(value = "1234567", type = Unknown(actual = "bp_passport_short_code")),
            expectedConvertedUnknownValue = "unknown"
        ),
        testCase(
            fallback = {
              it.value
                  .split("")
                  .filter { part -> part.isNotBlank() }
                  .joinToString("-")
            },
            unknownIdentifier = Identifier(value = "1234567", type = Unknown(actual = "bp_passport_short_code")),
            expectedConvertedUnknownValue = "1-2-3-4-5-6-7"
        )
    )
  }


  private class StubIdentifierToTextConverter(
      private val convertValueAction: (Identifier) -> String = { throw UnsupportedOperationException() }
  ) : IdentifierToTextConverter {

    override fun convertValue(identifier: Identifier) = convertValueAction(identifier)
  }
}
