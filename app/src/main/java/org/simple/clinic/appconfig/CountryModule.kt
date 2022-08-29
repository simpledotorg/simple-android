package org.simple.clinic.appconfig

import dagger.Module
import dagger.Provides

@Module
object CountryModule {

  @Provides
  fun providesCountry(
      appConfigRepository: AppConfigRepository
  ): Country {
    val selectedCountry = appConfigRepository.currentCountry()

    requireNotNull(selectedCountry) { "There is no stored country available!" }

    return selectedCountry
  }

  @Provides
  fun providesDeployment(
      appConfigRepository: AppConfigRepository
  ): Deployment {
    val selectedDeployment = appConfigRepository.currentDeployment()

    requireNotNull(selectedDeployment) { "There is no stored deployment available!" }

    return selectedDeployment
  }
}
