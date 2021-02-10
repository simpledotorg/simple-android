package org.simple.clinic.drugs.selection

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPrescribeddrugsProtocolDrugBinding
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.ProtocolDrugClicked
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone

data class ProtocolDrugListItem(
    val id: Int,
    val drugName: String,
    val prescribedDrug: PrescribedDrug?,
    val hideDivider: Boolean
) : GroupieItemWithUiEvents<ListPrescribeddrugsProtocolDrugBinding>(adapterId = id.toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun initializeViewBinding(view: View): ListPrescribeddrugsProtocolDrugBinding {
    return ListPrescribeddrugsProtocolDrugBinding.bind(view)
  }

  override fun bind(viewBinding: ListPrescribeddrugsProtocolDrugBinding, position: Int) {
    viewBinding.protocoldrugItemName.text = drugName

    viewBinding.protocoldrugItemDosage.visibleOrGone(prescribedDrug != null)
    viewBinding.protocoldrugItemDosage.text = prescribedDrug?.dosage

    viewBinding.root.setOnClickListener {
      uiEvents.onNext(ProtocolDrugClicked(drugName, prescribedDrug))
    }
    viewBinding.protocoldrugItemDivider.visibility = if (hideDivider) GONE else VISIBLE
  }
}
