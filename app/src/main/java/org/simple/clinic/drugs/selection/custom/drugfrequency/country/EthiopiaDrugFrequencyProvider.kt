package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS

class EthiopiaDrugFrequencyProvider : DrugFrequencyProvider {
  override fun provide(resources: Resources): List<DrugFrequencyChoiceItem> {
    return listOf(
        DrugFrequencyChoiceItem(drugFrequency = null, labelResId = R.string.custom_drug_entry_sheet_frequency_none),
        DrugFrequencyChoiceItem(drugFrequency = OD, labelResId = R.string.custom_drug_entry_sheet_frequency_ethiopia_PD),
        DrugFrequencyChoiceItem(drugFrequency = BD, labelResId = R.string.custom_drug_entry_sheet_frequency_ethiopia_BID),
        DrugFrequencyChoiceItem(drugFrequency = TDS, labelResId = R.string.custom_drug_entry_sheet_frequency_ethiopia_TID),
        DrugFrequencyChoiceItem(drugFrequency = QDS, labelResId = R.string.custom_drug_entry_sheet_frequency_ethiopia_QID)
    )
  }
}
