package org.simple.clinic.util.identifierdisplay

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter.IdentifierDisplayFormatter

@RunWith(JUnitParamsRunner::class)
class IdentifierDisplayAdapterTest {

  @Test
  @Parameters(method = "params for formatting values of the identifier")
  fun `the adapter should use the provided formatter for formatting the identifier value`(
      formatters: Map<Class<out Identifier.IdentifierType>, IdentifierDisplayFormatter>,
      identifiersToFormat: List<Identifier>,
      expected: List<String>
  ) {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(formatters = formatters)

    val formatted = identifiersToFormat.map(identifierDisplayAdapter::valueAsText)

    assertThat(formatted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for formatting values of the identifier`(): List<List<Any>> {
    fun testCase(
        formatters: Map<Class<out Identifier.IdentifierType>, IdentifierDisplayFormatter>,
        identifiersToFormat: List<Identifier>,
        expected: List<String>
    ): List<Any> {
      return listOf(formatters, identifiersToFormat, expected)
    }

    return listOf(
        testCase(
            formatters = mapOf(
                BpPassport::class.java to StubIdentifierDisplayFormatter(formatValueAction = { "bp_passport_${it.value}" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = BpPassport),
                Identifier(value = "id_3", type = BpPassport),
                Identifier(value = "id_2", type = BpPassport)
            ),
            expected = listOf("bp_passport_id_1", "bp_passport_id_3", "bp_passport_id_2")
        ),
        testCase(
            formatters = mapOf(
                Unknown::class.java to StubIdentifierDisplayFormatter(formatValueAction = { "unknown_${it.value}" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_2", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_3", type = Unknown(actual = "bp_passport_shortcode"))
            ),
            expected = listOf("unknown_id_1", "unknown_id_2", "unknown_id_3")
        ),
        testCase(
            formatters = mapOf(
                Unknown::class.java to StubIdentifierDisplayFormatter(formatValueAction = { "unknown ${it.value}" }),
                BpPassport::class.java to StubIdentifierDisplayFormatter(formatValueAction = { "bppassport ${it.value}" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_2", type = BpPassport),
                Identifier(value = "id_3", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_4", type = BpPassport)
            ),
            expected = listOf("unknown id_1", "bppassport id_2", "unknown id_3", "bppassport id_4")
        )
    )
  }

  @Test
  @Parameters(method = "params for formatting types of the identifier")
  fun `the adapter should use the provided formatter for formatting the identifier type`(
      formatters: Map<Class<out Identifier.IdentifierType>, IdentifierDisplayFormatter>,
      identifiersToFormat: List<Identifier>,
      expected: List<String>
  ) {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(formatters = formatters)

    val formatted = identifiersToFormat.map(identifierDisplayAdapter::typeAsText)

    assertThat(formatted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for formatting types of the identifier`(): List<List<Any>> {
    fun testCase(
        formatters: Map<Class<out Identifier.IdentifierType>, IdentifierDisplayFormatter>,
        identifiersToFormat: List<Identifier>,
        expected: List<String>
    ): List<Any> {
      return listOf(formatters, identifiersToFormat, expected)
    }

    return listOf(
        testCase(
            formatters = mapOf(
                BpPassport::class.java to StubIdentifierDisplayFormatter(formatTypeAction = { "bp_passport" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = BpPassport),
                Identifier(value = "id_3", type = BpPassport),
                Identifier(value = "id_2", type = BpPassport)
            ),
            expected = listOf("bp_passport", "bp_passport", "bp_passport")
        ),
        testCase(
            formatters = mapOf(
                Unknown::class.java to StubIdentifierDisplayFormatter(formatTypeAction = { "unknown" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_2", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_3", type = Unknown(actual = "bp_passport_shortcode"))
            ),
            expected = listOf("unknown", "unknown", "unknown")
        ),
        testCase(
            formatters = mapOf(
                Unknown::class.java to StubIdentifierDisplayFormatter(formatTypeAction = { "unknown type" }),
                BpPassport::class.java to StubIdentifierDisplayFormatter(formatTypeAction = { "bppassport" })
            ),
            identifiersToFormat = listOf(
                Identifier(value = "id_1", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_2", type = BpPassport),
                Identifier(value = "id_3", type = Unknown(actual = "bp_passport_shortcode")),
                Identifier(value = "id_4", type = BpPassport)
            ),
            expected = listOf("unknown type", "bppassport", "unknown type", "bppassport")
        )
    )
  }

  private class StubIdentifierDisplayFormatter(
      private val formatValueAction: (Identifier) -> String = { throw UnsupportedOperationException() },
      private val formatTypeAction: (Identifier) -> String = { throw UnsupportedOperationException() }
  ) : IdentifierDisplayFormatter {

    override fun formatValue(identifier: Identifier) = formatValueAction(identifier)

    override fun formatType(identifier: Identifier) = formatTypeAction(identifier)
  }
}
