package org.simple.clinic.teleconsultlog.shareprescription

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemTeleconsultSharePrescriptionMedicineBinding
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.Duration

data class TeleconsultSharePrescriptionItem(
    val prescribedDrug: PrescribedDrug,
    val defaultDuration: Duration,
    val defaultFrequency: MedicineFrequency
) : ItemAdapter.Item<UiEvent> {

  companion object {
    fun from(
        medicines: List<PrescribedDrug>,
        defaultDuration: Duration,
        defaultFrequency: MedicineFrequency
    ): List<TeleconsultSharePrescriptionItem> {
      return medicines
          .map { prescribedDrug ->
            TeleconsultSharePrescriptionItem(
                prescribedDrug = prescribedDrug,
                defaultDuration = defaultDuration,
                defaultFrequency = defaultFrequency
            )
          }
    }
  }

  override fun layoutResId(): Int = R.layout.list_item_teleconsult_share_prescription_medicine

  override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
    val binding = holder.binding as ListItemTeleconsultSharePrescriptionMedicineBinding
    val context = holder.itemView.context

    val frequency = prescribedDrug.frequency ?: defaultFrequency
    val durationInDays = prescribedDrug.durationInDays ?: defaultDuration.toDays().toInt()

    binding.medicineDetailTextView.text = context.getString(
        R.string.list_item_teleconsult_share_prescription_medicines_name,
        prescribedDrug.name,
        prescribedDrug.dosage,
        frequency,
        durationInDays.toString()
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
