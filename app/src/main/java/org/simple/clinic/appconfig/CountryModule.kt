package org.simple.clinic.appconfig

import dagger.Module
import dagger.Provides
import org.simple.clinic.util.toNullable

@Module
object CountryModule {

  @Provides
  fun providesCountry(appConfigRepository: AppConfigRepository): Country {
    val selectedCountry = appConfigRepository.currentCountry().toNullable()

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
}
