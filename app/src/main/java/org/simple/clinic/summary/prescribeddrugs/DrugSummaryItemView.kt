package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.simple.clinic.databinding.PatientsummaryDrugItemContentBinding
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

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

  fun render(
      drugName: String,
      drugDosage: String?,
      drugFrequency: MedicineFrequency?,
      drugDate: String
  ) {
    val drugWithDosageAndFrequency = listOfNotNull(drugName, drugDosage, drugFrequency)
        .joinToString(separator = " ")

    prescribedDrugName.text = drugWithDosageAndFrequency
    prescribedDrugDate.text = drugDate
  }
}
