package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS

class EthiopiaDrugFrequencyProvider : DrugFrequencyProvider {
  override fun provide(resources: Resources): Map<DrugFrequency?, DrugFrequencyLabel> {
    return mapOf(
        null to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_none)),
        OD to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_ethiopia_PD)),
        BD to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_ethiopia_BID)),
        TDS to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_ethiopia_TID)),
        QDS to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_ethiopia_QID))
    )
  }
}
