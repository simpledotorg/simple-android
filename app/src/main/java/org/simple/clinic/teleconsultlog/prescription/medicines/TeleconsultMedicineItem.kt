package org.simple.clinic.teleconsultlog.prescription.medicines

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_item_teleconsult_medicine.*
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
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

  override fun render(holder: ViewHolderX, subject: Subject<TeleconsultMedicineItemEvent>) {
    val context = holder.itemView.context
    holder.medicineNameTextView.text = context.getString(
        R.string.list_item_teleconsult_medicines_name,
        prescribedDrug.name,
        prescribedDrug.dosage
    )

    val frequency = prescribedDrug.frequency ?: defaultFrequency
    holder.medicineFrequencyButton.text = frequency.toString()

    val durationInDays = prescribedDrug.durationInDays ?: defaultDuration.toDays().toInt()
    holder.medicineDurationButton.text = context.resources.getQuantityString(
        R.plurals.list_item_teleconsult_medicine_duration,
        durationInDays,
        durationInDays.toString()
    )

    holder.medicineFrequencyButton.setOnClickListener {
      subject.onNext(DrugFrequencyButtonClicked(prescribedDrug))
    }

    holder.medicineDurationButton.setOnClickListener {
      subject.onNext(DrugDurationButtonClicked(prescribedDrug))
    }
  }
}

class TeleconsultMedicineDiffCallback : DiffUtil.ItemCallback<TeleconsultMedicineItem>() {
  override fun areItemsTheSame(oldItem: TeleconsultMedicineItem, newItem: TeleconsultMedicineItem): Boolean {
    return oldItem.prescribedDrug.uuid == newItem.prescribedDrug.uuid
  }

  override fun areContentsTheSame(oldItem: TeleconsultMedicineItem, newItem: TeleconsultMedicineItem): Boolean {
    return oldItem == newItem
  }
}
