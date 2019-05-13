package org.simple.clinic.drugs.selection.dosage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class DosageAdapter @Inject constructor() : ListAdapter<DosageListItem, DosageViewHolder>(DosageDiffer()) {

  val itemClicks: PublishSubject<UiEvent> = PublishSubject.create()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DosageViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.prescribed_drug_with_dosage_list_item, parent, false)
    return DosageViewHolder(layout, itemClicks)
  }

  override fun onBindViewHolder(holder: DosageViewHolder, position: Int) {
    holder.dosageItem = getItem(position)
    holder.render()
  }
}

class DosageViewHolder(itemView: View, itemClicks: Subject<UiEvent>) : RecyclerView.ViewHolder(itemView) {

  private val dosageTextView by bindView<TextView>(R.id.prescribed_drug_list_item_dosage_name)
  private val dividerView by bindView<View>(R.id.prescribed_drug_list_item_divider)

  lateinit var dosageItem: DosageListItem

  init {
    itemView.setOnClickListener {
      itemClicks.onNext(DosageItemClicked(dosageItem.dosageOption))
    }
  }

  fun render() {
    val dosageType = dosageItem.dosageOption
    when (dosageType) {
      is DosageOption.None -> {
        dosageTextView.text = itemView.context.getString(R.string.prescribed_drugs_dosage_none)
        dividerView.visibility = View.GONE
      }
      is DosageOption.Dosage -> {
        dosageTextView.text = dosageType.protocolDrug.dosage
        dividerView.visibility = View.VISIBLE
      }
    }.exhaustive()
  }
}

data class DosageListItem(val dosageOption: DosageOption)

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
