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
}
