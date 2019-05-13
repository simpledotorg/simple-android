package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.facility.Facility

sealed class FacilityListItem {

  sealed class Header: FacilityListItem() {
    abstract val hasSpacingWithPreviousSection: Boolean
    data class SuggestedFacilities(override val hasSpacingWithPreviousSection: Boolean) : Header()
    data class AllFacilities(override val hasSpacingWithPreviousSection: Boolean) : Header()
  }

  data class FacilityOption(
      val facility: Facility,
      val name: Name,
      val address: Address
  ): FacilityListItem() {

    sealed class Name(open val text: String) {
      data class Highlighted(override val text: String, val highlightStart: Int, val highlightEnd: Int) : Name(text)
      data class Plain(override val text: String) : Name(text)
    }

    sealed class Address {
      data class WithStreet(val street: String, val district: String, val state: String) : Address()
      data class WithoutStreet(val district: String, val state: String) : Address()
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
}

