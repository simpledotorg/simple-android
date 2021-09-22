package org.simple.clinic.drugs.selection.custom.drugfrequency.country

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS

class CommonDrugFrequencyProvider : DrugFrequencyProvider {
  override fun provide(resources: Resources): Map<DrugFrequency?, DrugFrequencyLabel> {
    return mapOf(
        null to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_none)),
        OD to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_OD)),
        BD to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_BD)),
        TDS to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_TDS)),
        QDS to DrugFrequencyLabel(resources.getString(R.string.custom_drug_entry_sheet_frequency_QDS))
    )
  }
}
