package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.CountryV2
import org.simple.clinic.appconfig.CountryV2.Companion.ETHIOPIA

@Module
class DrugFrequencyFactoryModule {

  @Provides
  fun provideDrugFrequencyProvider(country: CountryV2): DrugFrequencyProvider {
    return when (country.isoCountryCode) {
      ETHIOPIA -> EthiopiaDrugFrequencyProvider()
      else -> CommonDrugFrequencyProvider()
    }
  }
}
