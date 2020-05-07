package org.simple.clinic.drugs.selection.dosage

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.prescribed_drug_with_dosage_list_item.*
import org.simple.clinic.R
import org.simple.clinic.drugs.selection.dosage.DosageListItem.WithDosage
import org.simple.clinic.drugs.selection.dosage.DosageListItem.WithoutDosage
import org.simple.clinic.drugs.selection.dosage.DosageOption.Dosage
import org.simple.clinic.drugs.selection.dosage.DosageOption.None
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX

sealed class DosageListItem(val dosageOption: DosageOption) : ItemAdapter.Item<UiEvent> {

  companion object {
    fun from(protocolDrugs: List<ProtocolDrug>): List<DosageListItem> {
      return protocolDrugs
          .map(::WithDosage)
          .plus(WithoutDosage)
    }
  }

  data class WithDosage(val protocolDrug: ProtocolDrug) : DosageListItem(Dosage(protocolDrug)) {

    override fun layoutResId() = R.layout.prescribed_drug_with_dosage_list_item

    override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
      holder.dosageTextView.text = protocolDrug.dosage
      holder.itemView.setOnClickListener { subject.onNext(DosageItemClicked(dosageOption)) }
    }
  }

  object WithoutDosage : DosageListItem(None) {

    override fun layoutResId() = R.layout.prescribed_drug_with_dosage_list_item

    override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
      holder.dosageTextView.text = holder.itemView.context.getString(R.string.prescribed_drugs_dosage_none)
      holder.itemView.setOnClickListener { subject.onNext(DosageItemClicked(dosageOption)) }
    }
  }
}

class DosageDiffer : DiffUtil.ItemCallback<DosageListItem>() {
  override fun areItemsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return when {
      oldItem is WithDosage && newItem is WithDosage -> oldItem.protocolDrug.uuid == newItem.protocolDrug.uuid
      oldItem is WithoutDosage && newItem is WithoutDosage -> true
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return oldItem == newItem
  }
}
