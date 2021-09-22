package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Country.Companion.ETHIOPIA
import org.simple.clinic.drugs.search.DrugFrequency

@Module
class DrugFrequencyModule {

  @Provides
  fun provideDrugFrequencyProvider(country: Country): DrugFrequencyProvider {
    return when (country.isoCountryCode) {
      ETHIOPIA -> EthiopiaDrugFrequencyProvider()
      else -> CommonDrugFrequencyProvider()
    }
  }

  @Provides
  @JvmSuppressWildcards
  fun provideDrugFrequencyToLabelMap(
      provider: DrugFrequencyProvider,
      resources: Resources
  ): Map<DrugFrequency?, DrugFrequencyLabel> {
    return provider.provide(resources)
  }
}
