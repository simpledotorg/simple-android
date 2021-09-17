package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import java.net.URI

@Module
object CountryModule {

  @Provides
  fun providesCountry(
      appConfigRepository: AppConfigRepository,
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ): Country {
    createV2CountryIfOldCountryIsPresent(appConfigRepository, rxSharedPreferences, moshi)

    val selectedCountry = appConfigRepository.currentCountry()

    requireNotNull(selectedCountry) { "There is no stored country available!" }

    return selectedCountry
  }

  @Provides
  fun providesDeployment(
      appConfigRepository: AppConfigRepository,
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ): Deployment {
    createDeploymentIfOldCountryIsPresent(appConfigRepository, rxSharedPreferences, moshi)

    val selectedDeployment = appConfigRepository.currentDeployment()

    requireNotNull(selectedDeployment) { "There is no stored deployment available!" }

    return selectedDeployment
  }

  private fun createDeploymentIfOldCountryIsPresent(
      appConfigRepository: AppConfigRepository,
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ) {
    val selectedOldCountryPreference = rxSharedPreferences.getString("preference_selected_country_v1")
    val selectedDeployment = appConfigRepository.currentDeployment()

    if (selectedOldCountryPreference.isSet && selectedDeployment == null) {
      val selectedOldCountry = parseOldCountry(moshi, selectedOldCountryPreference.get())
      // Since V1 country doesn't have deployment names, we are going with country name
      val deployment = Deployment(
          displayName = selectedOldCountry["display_name"]!!,
          endPoint = URI.create(selectedOldCountry["endpoint"]!!)
      )
      appConfigRepository.saveDeployment(deployment)
    }
  }

  private fun createV2CountryIfOldCountryIsPresent(
      appConfigRepository: AppConfigRepository,
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ) {
    val selectedOldCountryPreference = rxSharedPreferences.getString("preference_selected_country_v1")
    val selectedNewCountry = appConfigRepository.currentCountry()

    if (selectedOldCountryPreference.isSet && selectedNewCountry == null) {
      val selectedOldCountry = parseOldCountry(moshi, selectedOldCountryPreference.get())
      // Since V1 country doesn't have deployment names, we are going with country name
      val deployment = Deployment(
          displayName = selectedOldCountry["display_name"]!!,
          endPoint = URI.create(selectedOldCountry["endpoint"]!!)
      )
      val country = Country(
          isoCountryCode = selectedOldCountry["country_code"]!!,
          displayName = selectedOldCountry["display_name"]!!,
          isdCode = selectedOldCountry["isd_code"]!!,
          deployments = listOf(deployment)
      )
      appConfigRepository.saveDeployment(deployment)
      appConfigRepository.saveCurrentCountry(country)

      selectedOldCountryPreference.delete()
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun parseOldCountry(moshi: Moshi, json: String): Map<String, String> {
    val selectedOldCountryAdapter = moshi.adapter(Object::class.java)

    return selectedOldCountryAdapter.fromJson(json) as Map<String, String>
  }
}
