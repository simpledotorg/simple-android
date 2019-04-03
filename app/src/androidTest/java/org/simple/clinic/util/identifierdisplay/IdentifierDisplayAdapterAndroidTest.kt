package org.simple.clinic.util.identifierdisplay

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class IdentifierDisplayAdapterAndroidTest {

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun all_types_of_identifiers_should_be_converted_for_display_correctly() {
    val identifiersAndExpectedDisplayCharSequence = Identifier
        .IdentifierType
        .values()
        .plus(Unknown(actual = "bppassport_short_code"))
        .map {
          when (it) {
            BpPassport -> Identifier(value = "1e8e3b37-cadd-4010-a4f8-14522bdf64a3", type = BpPassport) to "183 3740"
            is Unknown -> Identifier(value = "1234567", type = Unknown(actual = "bppassport_short_code")) to "1234567"
          }
        }

    identifiersAndExpectedDisplayCharSequence
        .forEach { (identifier, expected) ->
          val charSequence = identifierDisplayAdapter.toCharSequence(identifier)
          assertThat(charSequence).isEqualTo(expected)
        }
  }
}
