package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.squareup.moshi.Moshi
import org.junit.Before
import org.junit.Test
import java.util.Optional

class LoadV1CountryTest {

  private val countryPreference = mock<Preference<String>>()
  private val sharedPreferences = mock<RxSharedPreferences>()
  private val moshi = Moshi.Builder().build()

  private val loadV1Country = LoadV1Country(sharedPreferences, moshi)

  @Before
  fun setUp() {
    whenever(sharedPreferences.getString("preference_selected_country_v1")).thenReturn(countryPreference)
  }

  @Test
  fun `when the v1 country is present, the country should be loaded`() {
    // given
    val storedCountryJson = """
      |{
      | "country_code": "IN",
      | "endpoint": "https://api.simple.org/api/v1",
      | "display_name": "India",
      | "isd_code": "91"
      |}
    """.trimMargin()

    whenever(countryPreference.isSet).thenReturn(true)
    whenever(countryPreference.get()).thenReturn(storedCountryJson)

    // when
    val savedV1Country = loadV1Country.load()

    // then
    val expectedSavedCountry = mapOf(
        "country_code" to "IN",
        "endpoint" to "https://api.simple.org/api/v1",
        "display_name" to "India",
        "isd_code" to "91"
    )
    assertThat(savedV1Country).isEqualTo(Optional.of(expectedSavedCountry))
  }

  @Test
  fun `when the v1 country is absent, an empty country should be loaded`() {
    // given
    whenever(countryPreference.isSet).thenReturn(false)

    // when
    val savedV1Country = loadV1Country.load()

    // then
    assertThat(savedV1Country).isEqualTo(Optional.empty<CountryV1>())
  }
}
