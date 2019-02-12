package org.simple.clinic.facility.change

import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.facility.Facility

data class FacilityListItem(
    val facility: Facility,
    val name: Name,
    val address: Address
) {

  sealed class Name(open val text: String) {
    data class Highlighted(override val text: String, val highlightStart: Int, val highlightEnd: Int) : Name(text)
    data class Plain(override val text: String) : Name(text)
  }

  sealed class Address {
    data class WithStreet(val street: String, val district: String, val state: String) : Address()
    data class WithoutStreet(val district: String, val state: String) : Address()
  }

  class Differ : DiffUtil.ItemCallback<FacilityListItem>() {
    override fun areItemsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem.facility.uuid == newItem.facility.uuid
    override fun areContentsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem == newItem
  }
}
