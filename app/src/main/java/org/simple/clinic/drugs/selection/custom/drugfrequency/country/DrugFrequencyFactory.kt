package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import org.simple.clinic.drugs.search.DrugFrequency
import javax.inject.Inject

class DrugFrequencyFactory @Inject constructor(
    private val drugFrequencyProvider: DrugFrequencyProvider,
    private val resources: Resources
) {
  fun provideFields(): Map<DrugFrequency?, DrugFrequencyLabel> = drugFrequencyProvider.provide(resources)
}
