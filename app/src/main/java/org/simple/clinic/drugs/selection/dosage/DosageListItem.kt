package org.simple.clinic.drugs.selection.dosage

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.prescribed_drug_with_dosage_list_item.*
import org.simple.clinic.R
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX

data class DosageListItem(val dosageOption: DosageOption) : ItemAdapter.Item<UiEvent> {

  override fun layoutResId() = R.layout.prescribed_drug_with_dosage_list_item

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    val dosageOption = dosageOption
    when (dosageOption) {
      is DosageOption.None -> {
        holder.dosageTextView.text = holder.itemView.context.getString(R.string.prescribed_drugs_dosage_none)
        holder.dividerView.visibility = View.GONE
      }
      is DosageOption.Dosage -> {
        holder.dosageTextView.text = dosageOption.protocolDrug.dosage
        holder.dividerView.visibility = View.VISIBLE
      }
    }.exhaustive()

    holder.itemView.setOnClickListener { subject.onNext(DosageItemClicked(dosageOption)) }
  }
}

class DosageDiffer : DiffUtil.ItemCallback<DosageListItem>() {
  override fun areItemsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return if (oldItem.dosageOption is DosageOption.Dosage && newItem.dosageOption is DosageOption.Dosage) {
      oldItem.dosageOption.protocolDrug.uuid == newItem.dosageOption.protocolDrug.uuid
    } else {
      false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return oldItem.dosageOption == newItem.dosageOption
  }
}
