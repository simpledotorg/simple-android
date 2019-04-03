package org.simple.clinic.util.identifierdisplay

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter.IdentifierToTextConverter

class IdentifierDisplayAdapterTest {

  @Test
  fun `the adapter should use the provided converter for converting the identifier`() {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(
        mapOf(
            BpPassport to object : IdentifierToTextConverter {
              override fun convert(identifier: Identifier): CharSequence {
                return "bp_passport_${identifier.value}"
              }
            }
        )
    )
    val identifiers = listOf("id_1", "id_2", "id_3").map { Identifier(value = it, type = BpPassport) }

    val convertedForDisplay = identifiers.map(identifierDisplayAdapter::toCharSequence)

    val expected = listOf("bp_passport_id_1", "bp_passport_id_2", "bp_passport_id_3")

    assertThat(convertedForDisplay).isEqualTo(expected)
  }

  @Test
  fun `when a converter is not present, the adapter should return the identifer as is`() {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(emptyMap())

    val convertedForDisplay = identifierDisplayAdapter.toCharSequence(Identifier(value = "identifier", type = BpPassport))

    assertThat(convertedForDisplay).isEqualTo("identifier")
  }

  @Test
  fun `unknown identifiers should be converted to their value when displaying`() {
    val identifierDisplayAdapter = IdentifierDisplayAdapter(
        mapOf(
            BpPassport to object : IdentifierToTextConverter {
              override fun convert(identifier: Identifier): CharSequence {
                return "bp_passport_${identifier.value}"
              }
            }
        )
    )

    val identifiers = listOf(
        Identifier(value = "id_1", type = BpPassport),
        Identifier(value = "id_2", type = Unknown("identifier_type_1")),
        Identifier(value = "id_3", type = BpPassport),
        Identifier(value = "id_4", type = Unknown("identifier_type_2"))
    )

    val convertedForDisplay = identifiers.map(identifierDisplayAdapter::toCharSequence)

    val expected = listOf("bp_passport_id_1", "id_2", "bp_passport_id_3", "id_4")

    assertThat(convertedForDisplay).isEqualTo(expected)
  }
}
