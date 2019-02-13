package org.simple.clinic.registration.facility

 import android.text.Spannable
 import android.text.SpannableStringBuilder
 import android.text.style.ForegroundColorSpan
 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import android.widget.TextView
 import androidx.core.content.ContextCompat
 import androidx.recyclerview.widget.ListAdapter
 import com.xwray.groupie.ViewHolder
 import io.reactivex.subjects.PublishSubject
 import kotterknife.bindView
 import org.simple.clinic.R
 import org.simple.clinic.facility.Facility
 import org.simple.clinic.facility.change.FacilityListItem
 import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Address
 import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name
 import org.simple.clinic.util.exhaustive

/**
 * FYI: We tried using Groupie for facility screen, but it was resulting in a weird
 * error where a CheckBox click was leading to callbacks from two CheckBoxes in two rows.
 */
class FacilitiesAdapter : ListAdapter<FacilityListItem, FacilityViewHolder>(FacilityListItem.Differ()) {

  companion object {
    const val VIEW_TYPE_HEADER = 1
    const val VIEW_TYPE_FACILITY_OPTION = 2
  }

  val facilityClicks = PublishSubject.create<Facility>()!!

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
    return when (viewType) {
      VIEW_TYPE_HEADER -> {
        TODO()
      }
      VIEW_TYPE_FACILITY_OPTION -> {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_facility_selection, parent, false)
        val holder = FacilityViewHolder(layout)
        holder.itemView.setOnClickListener {
          facilityClicks.onNext(holder.facilityOption.facility)
        }
        holder
      }
      else -> throw AssertionError()
    }
  }

  override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
    val item = getItem(position)
    when (item) {
      is FacilityListItem.Header -> {
        TODO()
      }
      is FacilityListItem.FacilityOption -> {
        holder.facilityOption = item
        holder.render()
      }
    }.exhaustive()
  }

  override fun getItemId(position: Int): Long {
    // DiffUtil handles calculating item changes
    // even without stable IDs so this is fine.
    return position.toLong()
  }
}

class FacilityViewHolder(rootView: View) : ViewHolder(rootView) {
  private val nameTextView by bindView<TextView>(R.id.facility_item_name)
  private val addressTextView by bindView<TextView>(R.id.facility_item_address)

  lateinit var facilityOption: FacilityListItem.FacilityOption

  fun render() {
    val name = facilityOption.name
    when (name) {
      is Name.Highlighted -> {
        val highlightedName = SpannableStringBuilder(name.text)
        val highlightColor = ContextCompat.getColor(itemView.context, R.color.facility_search_query_highlight)
        highlightedName.setSpan(ForegroundColorSpan(highlightColor), name.highlightStart, name.highlightEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        nameTextView.text = highlightedName
      }
      is Name.Plain -> {
        nameTextView.text = name.text
      }
    }.exhaustive()

    val address = facilityOption.address
    addressTextView.text = when (address) {
      is Address.WithStreet -> {
        itemView.resources.getString(
            R.string.registrationfacilities_facility_address_with_street,
            address.street,
            address.district,
            address.state)
      }
      is Address.WithoutStreet -> {
        itemView.resources.getString(
            R.string.registrationfacilities_facility_address_without_street,
            address.district,
            address.state)
      }
    }
  }
}
