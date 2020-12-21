package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListFacilitySelectionHeaderBinding
import org.simple.clinic.databinding.ListFacilitySelectionOptionBinding
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name.Highlighted
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name.Plain
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.setTopMargin
import org.simple.clinic.widgets.setTopMarginRes

sealed class FacilityListItem : ItemAdapter.Item<FacilityListItem.FacilityItemClicked> {

  sealed class Header : FacilityListItem() {

    abstract val hasSpacingWithPreviousSection: Boolean

    data class SuggestedFacilities(override val hasSpacingWithPreviousSection: Boolean) : Header()
    data class AllFacilities(override val hasSpacingWithPreviousSection: Boolean) : Header()

    override fun layoutResId(): Int {
      return R.layout.list_facility_selection_header
    }

    override fun render(holder: BindingViewHolder, subject: Subject<FacilityItemClicked>) {
      val binding = holder.binding as ListFacilitySelectionHeaderBinding

      binding.headerNameTextView.setText(when (this) {
        is SuggestedFacilities -> R.string.facilitypicker_header_suggested_facilities
        is AllFacilities -> R.string.facilitypicker_header_all_facilities
      })

      if (hasSpacingWithPreviousSection) {
        holder.itemView.setTopMarginRes(R.dimen.registrationfacilities_header_top_spacing_with_previous_section)
      } else {
        holder.itemView.setTopMargin(0)
      }
    }
  }

  data class FacilityOption(
      val facility: Facility,
      val name: Name,
      val address: Address
  ) : FacilityListItem() {

    sealed class Name(open val text: String) {
      data class Highlighted(override val text: String, val highlightStart: Int, val highlightEnd: Int) : Name(text)
      data class Plain(override val text: String) : Name(text)
    }

    sealed class Address {
      data class WithStreet(val street: String, val district: String, val state: String) : Address()
      data class WithoutStreet(val district: String, val state: String) : Address()
    }

    override fun layoutResId(): Int {
      return R.layout.list_facility_selection_option
    }

    override fun render(holder: BindingViewHolder, subject: Subject<FacilityItemClicked>) {
      val binding = holder.binding as ListFacilitySelectionOptionBinding

      when (name) {
        is Highlighted -> {
          val highlightedName = SpannableStringBuilder(name.text)
          val highlightColor = ContextCompat.getColor(holder.itemView.context, R.color.facility_search_query_highlight)
          highlightedName.setSpan(BackgroundColorSpan(highlightColor), name.highlightStart, name.highlightEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          binding.facilityNameTextView.text = highlightedName
        }
        is Plain -> {
          binding.facilityNameTextView.text = name.text
        }
      }.exhaustive()

      binding.facilityAddressTextView.text = when (address) {
        is Address.WithStreet -> {
          holder.itemView.resources.getString(
              R.string.facilitypicker_facility_address_with_street,
              address.street,
              address.district,
              address.state)
        }
        is Address.WithoutStreet -> {
          holder.itemView.resources.getString(
              R.string.facilitypicker_facility_address_without_street,
              address.district,
              address.state)
        }
      }

      holder.itemView.setOnClickListener { subject.onNext(FacilityItemClicked(facility)) }
    }
  }

  class Differ : DiffUtil.ItemCallback<FacilityListItem>() {
    override fun areItemsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem): Boolean {
      return if (oldItem is FacilityOption && newItem is FacilityOption) {
        oldItem.facility.uuid == newItem.facility.uuid
      } else {
        oldItem == newItem
      }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem): Boolean {
      return oldItem == newItem
    }
  }

  data class FacilityItemClicked(val facility: Facility)
}

