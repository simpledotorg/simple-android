package org.simple.clinic.drugs.selectionv2.dosage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.util.exhaustive
import javax.inject.Inject

class DosageAdapter @Inject constructor() : ListAdapter<DosageListItem, DosageViewHolder>(PrescribedDosageDiffer()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DosageViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.prescribed_drug_with_dosage_list_item, parent, false)
    return DosageViewHolder(layout)
  }

  override fun onBindViewHolder(holder: DosageViewHolder, position: Int) {
    holder.dosageItem = getItem(position)
    holder.render()
  }
}

class DosageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val dosageTextView by bindView<TextView>(R.id.prescribed_drug_list_item_dosage_name)
  private val dividerView by bindView<View>(R.id.prescribed_drug_list_item_divider)

  lateinit var dosageItem: DosageListItem

  fun render() {
    val dosageType = dosageItem.dosageOption
    when (dosageType) {
      is DosageOption.None -> {
        dosageTextView.text = itemView.context.getString(R.string.prescribed_drugs_dosage_none)
        dividerView.visibility = View.GONE
      }
      is DosageOption.Dosage -> {
        dosageTextView.text = dosageType.dosage
        dividerView.visibility = View.VISIBLE
      }
    }.exhaustive()
  }
}

data class DosageListItem(val dosageOption: DosageOption)

class PrescribedDosageDiffer : DiffUtil.ItemCallback<DosageListItem>() {
  override fun areItemsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return if (oldItem.dosageOption is DosageOption.Dosage && newItem.dosageOption is DosageOption.Dosage) {
      oldItem.dosageOption.dosage == newItem.dosageOption.dosage
    } else {
      false
    }
  }

  override fun areContentsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return oldItem.dosageOption == newItem.dosageOption
  }
}
