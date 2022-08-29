package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import java.util.Optional
import javax.inject.Inject

typealias CountryV1 = Map<String, String>

class LoadV1Country @Inject constructor(
    private val sharedPreferences: RxSharedPreferences,
    private val moshi: Moshi
) {

  fun load(): Optional<CountryV1> {
    val storedCountryPreference = sharedPreferences.getString("preference_selected_country_v1")

    return if (storedCountryPreference.isSet)
      parseOldCountry(storedCountryPreference)
    else
      Optional.empty()
  }

  @Suppress("UNCHECKED_CAST")
  private fun parseOldCountry(storedCountryPreference: Preference<String>): Optional<CountryV1> {
    val selectedOldCountryAdapter = moshi.adapter(Object::class.java)
    return Optional.of(selectedOldCountryAdapter.fromJson(storedCountryPreference.get()) as CountryV1)
  }
}
