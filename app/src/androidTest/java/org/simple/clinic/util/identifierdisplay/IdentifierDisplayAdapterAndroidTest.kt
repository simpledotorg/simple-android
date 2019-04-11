package org.simple.clinic.util.identifierdisplay

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.businessid.Identifier
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
  fun all_known_types_of_adapters_must_be_registered_in_the_adapter() {
    val registeredIdentifierTypes = identifierDisplayAdapter.converters.keys

    val expected = Identifier.IdentifierType.values().toSet()

    // If this fails, you probably forgot to register an adapter for a newly added IdentifierType in
    // IdentifierDisplayAdapterModule
    assertThat(registeredIdentifierTypes).isEqualTo(expected)
  }
}
