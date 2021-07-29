package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

interface CustomDrugEntryUi {
  fun setDrugFrequency(drugFrequency: DrugFrequency)
  fun setDrugDosage(dosage: String)
  fun setDrugName(drugName: String)
  fun hideRemoveButton()
  fun setButtonTextAsAdd()
}
