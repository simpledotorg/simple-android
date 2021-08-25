package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Country.Companion.BANGLADESH
import org.simple.clinic.appconfig.Country.Companion.ETHIOPIA
import org.simple.clinic.appconfig.Country.Companion.INDIA
import org.simple.clinic.appconfig.Country.Companion.SRI_LANKA

@Module
class DrugFrequencyFactoryModule {

  @Provides
  fun provideDrugFrequencyProvider(country: Country): DrugFrequencyProvider {
    return when(val isoCountryCode = country.isoCountryCode){
      ETHIOPIA -> EthiopiaDrugFrequencyProvider()
      INDIA, BANGLADESH, SRI_LANKA -> CommonDrugFrequencyProvider()
      else -> throw IllegalArgumentException("Unknown country code: $isoCountryCode")
    }
  }
}
