package org.simple.clinic.appconfig

import dagger.Module
import dagger.Provides
import org.simple.clinic.util.toNullable

@Module
object CountryModule {

  @Provides
  fun providesCountry(appConfigRepository: AppConfigRepository): Country {
    createV2CountryIfOldCountryIsPresent(appConfigRepository)

    val selectedCountry = appConfigRepository.currentCountryV2()

    requireNotNull(selectedCountry) { "There is no stored country available!" }

    return selectedCountry
  }

  @Provides
  fun providesDeployment(appConfigRepository: AppConfigRepository): Deployment {
    createDeploymentIfOldCountryIsPresent(appConfigRepository)

    val selectedDeployment = appConfigRepository.currentDeployment()

    requireNotNull(selectedDeployment) { "There is no stored deployment available!" }

    return selectedDeployment
  }

  private fun createDeploymentIfOldCountryIsPresent(appConfigRepository: AppConfigRepository) {
    val selectedCountry = appConfigRepository.currentCountry().toNullable()
    val selectedDeployment = appConfigRepository.currentDeployment()

    if (selectedCountry != null && selectedDeployment == null) {
      // Since V1 country doesn't have deployment names, we are going with country name
      val deployment = Deployment(
          displayName = selectedCountry.displayName,
          endPoint = selectedCountry.endpoint
      )
      appConfigRepository.saveDeployment(deployment)
    }
  }

  private fun createV2CountryIfOldCountryIsPresent(appConfigRepository: AppConfigRepository) {
    val selectedOldCountry = appConfigRepository.currentCountry().toNullable()
    val selectedNewCountry = appConfigRepository.currentCountryV2()

    if (selectedOldCountry != null && selectedNewCountry == null) {
      // Since V1 country doesn't have deployment names, we are going with country name
      val deployment = Deployment(
          displayName = selectedOldCountry.displayName,
          endPoint = selectedOldCountry.endpoint
      )
      val country = Country(
          isoCountryCode = selectedOldCountry.isoCountryCode,
          displayName = selectedOldCountry.displayName,
          isdCode = selectedOldCountry.isdCode,
          deployments = listOf(deployment)
      )
      appConfigRepository.saveDeployment(deployment)
      appConfigRepository.saveCurrentCountry(country)
      appConfigRepository.deleteV1Country()
    }
  }
}
