package org.simple.clinic.teleconsultlog.shareprescription

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_item_teleconsult_share_prescription_medicine.*
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX

data class TeleconsultSharePrescriptionItem(
    val prescribedDrug: PrescribedDrug
) : ItemAdapter.Item<UiEvent> {

  companion object {
    fun from(
        medicines: List<PrescribedDrug>
    ): List<TeleconsultSharePrescriptionItem> {
      return medicines
          .map { prescribedDrug ->
            TeleconsultSharePrescriptionItem(
                prescribedDrug = prescribedDrug
            )
          }
    }
  }

  override fun layoutResId(): Int = R.layout.list_item_teleconsult_share_prescription_medicine

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    val context = holder.itemView.context

    holder.medicineDetailTextView.text = context.getString(
        R.string.list_item_teleconsult_share_prescription_medicines_name,
        prescribedDrug.name,
        prescribedDrug.dosage,
        prescribedDrug.frequency.toString(),
        prescribedDrug.durationInDays.toString()
    )
  }
}

class TeleconsultSharePrescriptionDiffCallback : DiffUtil.ItemCallback<TeleconsultSharePrescriptionItem>() {
  override fun areItemsTheSame(oldItem: TeleconsultSharePrescriptionItem, newItem: TeleconsultSharePrescriptionItem): Boolean {
    return oldItem.prescribedDrug.uuid == newItem.prescribedDrug.uuid
  }

  override fun areContentsTheSame(oldItem: TeleconsultSharePrescriptionItem, newItem: TeleconsultSharePrescriptionItem): Boolean {
    return oldItem == newItem
  }

}
