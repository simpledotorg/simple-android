package org.simple.clinic.registration.facility

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

/**
 * FYI: We tried using Groupie for facility screen, but it was resulting in a weird
 * error where a CheckBox click was leading to callbacks from two CheckBoxes in two rows.
 */
class FacilitiesAdapter : ListAdapter<FacilityListItem, FacilityViewHolder>(FacilityDiffer()) {

  init {
    setHasStableIds(true)
  }

  val uiEvents = PublishSubject.create<UiEvent>()!!

  var facilityItems: List<FacilityListItem> = emptyList()
    set(value) {
      field = value
      submitList(field)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_facility_selection, parent, false)
    return FacilityViewHolder(layout, uiEvents)
  }

  override fun getItemCount(): Int {
    return facilityItems.size
  }

  override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
    holder.item = facilityItems[position]
    holder.render()
  }

  override fun getItemId(position: Int): Long {
    return facilityItems[position].facility.uuid.hashCode().toLong()
  }

  fun update(facilityItems: List<FacilityListItem>) {
    this.facilityItems = facilityItems
    notifyDataSetChanged()
  }
}

data class FacilityListItem(
    val facility: Facility,
    val isSelected: Boolean
)

class FacilityViewHolder(rootView: View, uiEvents: Subject<UiEvent>) : ViewHolder(rootView) {
  private val selectionCheckbox by bindView<CheckBox>(R.id.facility_item_selection_checkbox)
  private val nameTextView by bindView<TextView>(R.id.facility_item_name)
  private val addressTextView by bindView<TextView>(R.id.facility_item_address)

  lateinit var item: FacilityListItem

  init {
    selectionCheckbox.setOnCheckedChangeListener { _, isChecked ->
      uiEvents.onNext(RegistrationFacilitySelectionChanged(item.facility, isChecked))
    }
    itemView.setOnClickListener {
      selectionCheckbox.performClick()
    }
  }

  fun render() {
    val facility = item.facility
    val isSelected = item.isSelected

    selectionCheckbox.isChecked = isSelected
    nameTextView.text = facility.name

    if (facility.streetAddress.isNullOrBlank()) {
      addressTextView.text = itemView.resources.getString(
          R.string.registrationfacilities_facility_address_without_street,
          facility.district,
          facility.state)
    } else {
      addressTextView.text = itemView.resources.getString(
          R.string.registrationfacilities_facility_address_with_street,
          facility.streetAddress,
          facility.district,
          facility.state)
    }
  }
}

class FacilityDiffer : DiffUtil.ItemCallback<FacilityListItem>() {
  override fun areItemsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem.facility.uuid == newItem.facility.uuid
  override fun areContentsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem == newItem
}
