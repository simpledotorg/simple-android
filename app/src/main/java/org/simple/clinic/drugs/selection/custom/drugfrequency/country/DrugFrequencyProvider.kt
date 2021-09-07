package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources

interface DrugFrequencyProvider {
  fun provide(resources: Resources): List<DrugFrequencyChoiceItem>
}
