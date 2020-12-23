package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.simple.clinic.R
import org.simple.clinic.databinding.PatientsummaryDrugItemContentBinding

class DrugSummaryItemView constructor(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {

  private var binding: PatientsummaryDrugItemContentBinding? = null

  private val prescribedDrugName
    get() = binding!!.prescribedDrugName

  private val prescribedDrugDate
    get() = binding!!.prescribedDrugDate

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = PatientsummaryDrugItemContentBinding.inflate(layoutInflater, this, true)
  }

  fun render(drugName: String, drugDosage: String, drugDate: String) {
    prescribedDrugName.text = context.getString(R.string.prescribeddrugsummary_item_name, drugName, drugDosage)
    prescribedDrugDate.text = drugDate
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
  }
}
