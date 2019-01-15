package org.simple.clinic.drugs.selectionv2.dosage

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class PrescribedDosageAdapter @Inject constructor() : ListAdapter<PrescribedDosageListItem, PrescribedDosageViewHolder>(PrescribedDosageDiffer()) {

  val itemClicks = PublishSubject.create<UiEvent>()!!

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescribedDosageViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.prescribed_drug_with_dosage_list_item, parent, false)
    return PrescribedDosageViewHolder(layout, itemClicks)
  }

  override fun onBindViewHolder(holder: PrescribedDosageViewHolder, position: Int) {
    holder.dosageItem = getItem(position)
    holder.render()
  }
}

class PrescribedDosageViewHolder(itemView: View, val itemClicks: Subject<UiEvent>) : RecyclerView.ViewHolder(itemView) {

  private val dosageTextView by bindView<TextView>(R.id.prescribed_drug_list_item_dosage_name)
  private val divider by bindView<View>(R.id.prescribed_drug_list_item_divider)

  lateinit var dosageItem: PrescribedDosageListItem

  init {
    itemView.setOnClickListener {
      itemClicks.onNext(DosageItemClicked(dosageItem.dosageType))
    }
  }

  fun render() {
    val dosageType = dosageItem.dosageType
    when (dosageType) {
      is DosageType.None -> {
        dosageTextView.text = itemView.context.getString(R.string.prescribed_drugs_dosage_none)
        divider.visibility = View.GONE
      }
      is DosageType.Dosage -> {
        dosageTextView.text = dosageType.dosage
        divider.visibility = View.VISIBLE
      }
    }
  }
}

data class PrescribedDosageListItem(val dosageType: DosageType)

class PrescribedDosageDiffer : DiffUtil.ItemCallback<PrescribedDosageListItem>() {
  override fun areItemsTheSame(oldItem: PrescribedDosageListItem, newItem: PrescribedDosageListItem): Boolean {
    return if (oldItem.dosageType is DosageType.Dosage && newItem.dosageType is DosageType.Dosage) {
      oldItem.dosageType.dosage == newItem.dosageType.dosage
    } else {
      false
    }
  }

  override fun areContentsTheSame(oldItem: PrescribedDosageListItem, newItem: PrescribedDosageListItem): Boolean {
    return oldItem.dosageType == newItem.dosageType
  }
}
