package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import javax.inject.Inject

class DrugFrequencyFactory @Inject constructor(
    private val drugFrequencyProvider: DrugFrequencyProvider,
    private val resources: Resources
) {
  fun provideFields(): List<DrugFrequencyChoiceItem> = drugFrequencyProvider.provide(resources)
}
