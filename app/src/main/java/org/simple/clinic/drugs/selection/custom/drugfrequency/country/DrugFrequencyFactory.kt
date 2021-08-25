package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import javax.inject.Inject

class DrugFrequencyFactory @Inject constructor(
    private val drugFrequencyProvider: DrugFrequencyProvider
) {
  fun provideFields() : List<DrugFrequencyChoiceItem> {
    return drugFrequencyProvider.provide()
  }
}
