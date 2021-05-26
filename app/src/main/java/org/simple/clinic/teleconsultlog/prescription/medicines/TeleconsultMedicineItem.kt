package org.simple.clinic.teleconsultlog.prescription.medicines

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemTeleconsultMedicineBinding
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.Duration

data class TeleconsultMedicineItem(
    val prescribedDrug: PrescribedDrug,
    val defaultDuration: Duration,
    val defaultFrequency: MedicineFrequency
) : ItemAdapter.Item<TeleconsultMedicineItemEvent> {

  companion object {

    fun from(
        medicines: List<PrescribedDrug>,
        defaultDuration: Duration,
        defaultFrequency: MedicineFrequency
    ): List<TeleconsultMedicineItem> {
      return medicines
          .map { prescribedDrug ->
            TeleconsultMedicineItem(
                prescribedDrug = prescribedDrug,
                defaultDuration = defaultDuration,
                defaultFrequency = defaultFrequency
            )
          }
    }
  }

  override fun layoutResId(): Int = R.layout.list_item_teleconsult_medicine

  override fun render(holder: BindingViewHolder, subject: Subject<TeleconsultMedicineItemEvent>) {
    val binding = holder.binding as ListItemTeleconsultMedicineBinding

    val context = holder.itemView.context
    binding.medicineNameTextView.text = context.getString(
        R.string.list_item_teleconsult_medicines_name,
        prescribedDrug.name,
        prescribedDrug.dosage
    )

    val frequency = prescribedDrug.frequency ?: defaultFrequency
    binding.medicineFrequencyButton.text = frequency.toString()

    val durationInDays = prescribedDrug.durationInDays ?: defaultDuration.toDays().toInt()
    binding.medicineDurationButton.text = context.resources.getQuantityString(
        R.plurals.list_item_teleconsult_medicine_duration,
        durationInDays,
        durationInDays.toString()
    )

    binding.medicineFrequencyButton.setOnClickListener {
      subject.onNext(DrugFrequencyButtonClicked(prescribedDrug))
    }

    binding.medicineDurationButton.setOnClickListener {
      subject.onNext(DrugDurationButtonClicked(prescribedDrug))
    }
  }
}

class TeleconsultMedicineDiffCallback : DiffUtil.ItemCallback<TeleconsultMedicineItem>() {
  override fun areItemsTheSame(
      oldItem: TeleconsultMedicineItem,
      newItem: TeleconsultMedicineItem
  ): Boolean {
    return oldItem.prescribedDrug.uuid == newItem.prescribedDrug.uuid
  }

  override fun areContentsTheSame(
      oldItem: TeleconsultMedicineItem,
      newItem: TeleconsultMedicineItem
  ): Boolean {
    return oldItem == newItem
  }
}
