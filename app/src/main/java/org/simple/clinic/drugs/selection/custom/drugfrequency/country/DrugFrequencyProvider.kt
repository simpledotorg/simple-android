package org.simple.clinic.drugs.selection.custom.drugfrequency.country

interface DrugFrequencyProvider {
  fun provide(): List<DrugFrequencyChoiceItem>
}
