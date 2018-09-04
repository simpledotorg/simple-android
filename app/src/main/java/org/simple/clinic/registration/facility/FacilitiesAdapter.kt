package org.simple.clinic.registration.facility

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.facility.Facility

/**
 * FYI: We tried using Groupie for facility screen, but it was resulting in a weird
 * error where a CheckBox click was leading to callbacks from two CheckBoxes in two rows.
 */
class FacilitiesAdapter : ListAdapter<Facility, FacilityViewHolder>(FacilityDiffer()) {

  val facilityClicks = PublishSubject.create<Facility>()!!

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_facility_selection, parent, false)
    return FacilityViewHolder(layout, facilityClicks)
  }

  override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
    holder.facility = getItem(position)
    holder.render()
  }

  override fun getItemId(position: Int): Long {
    // The data-set never changes, so this is fine.
    return position.toLong()
  }
}

class FacilityViewHolder(rootView: View, uiEvents: Subject<Facility>) : ViewHolder(rootView) {
  private val nameTextView by bindView<TextView>(R.id.facility_item_name)
  private val addressTextView by bindView<TextView>(R.id.facility_item_address)

  lateinit var facility: Facility

  init {
    itemView.setOnClickListener {
      uiEvents.onNext(facility)
    }
  }

  fun render() {
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

class FacilityDiffer : DiffUtil.ItemCallback<Facility>() {
  override fun areItemsTheSame(oldItem: Facility, newItem: Facility) = oldItem.uuid == newItem.uuid
  override fun areContentsTheSame(oldItem: Facility, newItem: Facility) = oldItem == newItem
}
