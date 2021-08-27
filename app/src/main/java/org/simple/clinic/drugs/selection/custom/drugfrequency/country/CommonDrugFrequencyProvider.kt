package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import org.simple.clinic.R
import org.simple.clinic.drugs.search.DrugFrequency

class CommonDrugFrequencyProvider : DrugFrequencyProvider {
  override fun provide(): List<DrugFrequencyChoiceItem> {
    return listOf(
        DrugFrequencyChoiceItem(drugFrequency = null, labelResId = R.string.custom_drug_entry_sheet_frequency_none),
        DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.OD, labelResId = R.string.custom_drug_entry_sheet_frequency_OD),
        DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.BD, labelResId = R.string.custom_drug_entry_sheet_frequency_BD),
        DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.TDS, labelResId = R.string.custom_drug_entry_sheet_frequency_TDS),
        DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.QDS, labelResId = R.string.custom_drug_entry_sheet_frequency_QDS)
    )
  }
}
