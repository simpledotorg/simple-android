package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Country.Companion.ETHIOPIA

@Module
class DrugFrequencyModule {

  @Provides
  fun provideDrugFrequencyProvider(country: Country): DrugFrequencyProvider {
    return when (country.isoCountryCode) {
      ETHIOPIA -> EthiopiaDrugFrequencyProvider()
      else -> CommonDrugFrequencyProvider()
    }
  }
}
