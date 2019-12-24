package org.simple.clinic.bloodsugar.selection.type

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_blood_sugar_type.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.widgets.visibleOrGone

typealias OnBloodSugarTypeSelected = (BloodSugarMeasurementType) -> Unit

class BloodSugarTypeAdapter(
    private val onBloodSugarTypeSelected: OnBloodSugarTypeSelected
) : ListAdapter<BloodSugarTypeListItem, BloodSugarTypeViewHolder>(BloodSugarTypeDiffer()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodSugarTypeViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_blood_sugar_type, parent, false)
    val viewHolder = BloodSugarTypeViewHolder(layout)
    viewHolder.itemView.setOnClickListener {
      onBloodSugarTypeSelected(viewHolder.bloodSugarTypeItem.measurementType)
    }
    return viewHolder
  }

  override fun onBindViewHolder(holder: BloodSugarTypeViewHolder, position: Int) {
    val showDividerIfNotLastItem = (position != itemCount - 1)

    holder.bloodSugarTypeItem = getItem(position)
    holder.render(showDividerIfNotLastItem)
  }
}

class BloodSugarTypeViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

  lateinit var bloodSugarTypeItem: BloodSugarTypeListItem

  fun render(isDividerVisible: Boolean) {
    itemName.text = bloodSugarTypeItem.name
    divider.visibleOrGone(isDividerVisible)
  }
}

data class BloodSugarTypeListItem(val name: String, val measurementType: BloodSugarMeasurementType)

class BloodSugarTypeDiffer : DiffUtil.ItemCallback<BloodSugarTypeListItem>() {
  override fun areItemsTheSame(oldItem: BloodSugarTypeListItem, newItem: BloodSugarTypeListItem): Boolean {
    return oldItem.measurementType == newItem.measurementType
  }

  override fun areContentsTheSame(oldItem: BloodSugarTypeListItem, newItem: BloodSugarTypeListItem): Boolean {
    return oldItem == newItem
  }
}
