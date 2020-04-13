package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_drug_item_content.view.*
import org.simple.clinic.R

class DrugSummaryItemView constructor(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {

  init {
    View.inflate(context, R.layout.patientsummary_drug_item_content, this)
  }

  fun render(drugName: String, drugDosage: String, drugDate: String) {
    prescribedDrugName.text = context.getString(R.string.prescribeddrugsummary_item_name, drugName, drugDosage)
    prescribedDrugDate.text = drugDate
  }
}
