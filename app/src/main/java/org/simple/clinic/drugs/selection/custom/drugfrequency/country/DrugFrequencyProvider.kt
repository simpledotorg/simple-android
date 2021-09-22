package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import org.simple.clinic.drugs.search.DrugFrequency

interface DrugFrequencyProvider {
  fun provide(resources: Resources): Map<DrugFrequency?, DrugFrequencyLabel>
}
