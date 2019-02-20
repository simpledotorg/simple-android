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
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Address
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.setTopMargin
import org.simple.clinic.widgets.setTopMarginRes
import org.simple.clinic.widgets.visibleOrGone

/**
 * FYI: We tried using Groupie for facility screen, but it was resulting in a weird
 * error where a CheckBox click was leading to callbacks from two CheckBoxes in two rows.
 */
class FacilitiesAdapter : ListAdapter<FacilityListItem, RecyclerView.ViewHolder>(FacilityListItem.Differ()) {

  companion object {
    const val VIEW_TYPE_HEADER = 1
    const val VIEW_TYPE_FACILITY_OPTION = 2
  }

  val facilityClicks = PublishSubject.create<Facility>()!!

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is FacilityListItem.Header -> VIEW_TYPE_HEADER
      is FacilityListItem.FacilityOption -> VIEW_TYPE_FACILITY_OPTION
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      VIEW_TYPE_HEADER -> {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_facility_selection_header, parent, false)
        FacilityHeaderViewHolder(layout)
      }
      VIEW_TYPE_FACILITY_OPTION -> {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.list_facility_selection_option, parent, false)
        val holder = FacilityOptionViewHolder(layout)
        holder.itemView.setOnClickListener {
          facilityClicks.onNext(holder.facilityOption.facility)
        }
        holder
      }
      else -> throw AssertionError()
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = getItem(position)
    when (item) {
      is FacilityListItem.Header -> {
        (holder as FacilityHeaderViewHolder).render(item)
        holder.setSpacingWithPreviousSectionVisible(item.hasSpacingWithPreviousSection)
      }
      is FacilityListItem.FacilityOption -> {
        (holder as FacilityOptionViewHolder).facilityOption = item
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

class FacilityHeaderViewHolder(rootView: View) : ViewHolder(rootView) {
  private val nameTextView by bindView<TextView>(R.id.facility_header_item_name)

  fun render(header: FacilityListItem.Header) {
    nameTextView.setText(when (header) {
      is FacilityListItem.Header.SuggestedFacilities -> R.string.registrationfacilities_header_suggested_facilities
      is FacilityListItem.Header.AllFacilities -> R.string.registrationfacilities_header_all_facilities
    })
  }

  fun setSpacingWithPreviousSectionVisible(visible: Boolean) {
    when {
      visible -> itemView.setTopMarginRes(R.dimen.registrationfacilities_header_top_spacing_with_previous_section)
      else -> itemView.setTopMargin(0)
    }
  }
}

class FacilityOptionViewHolder(rootView: View) : ViewHolder(rootView) {
  private val nameTextView by bindView<TextView>(R.id.facility_item_name)
  private val addressTextView by bindView<TextView>(R.id.facility_item_address)
  private val dividerView by bindView<View>(R.id.facility_item_divider)

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

    dividerView.visibility = if (facilityOption.showBottomDivider) View.VISIBLE else View.GONE
    dividerView.visibleOrGone(facilityOption.showBottomDivider)
  }
}
