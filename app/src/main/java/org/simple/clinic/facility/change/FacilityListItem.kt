package org.simple.clinic.facility.change

import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.facility.Facility

data class FacilityListItem(
    val facility: Facility,
    val name: Name,
    val address: Address
) {

  sealed class Name {
    data class Highlighted(val name: String, val highlightStart: Int, val highlightEnd: Int) : Name()
    data class Plain(val name: String) : Name()
  }

  sealed class Address {
    data class WithStreet(val street: String, val district: String, val state: String) : Address()
    data class WithoutStreet(val district: String, val state: String) : Address()
  }

  class Differ : DiffUtil.ItemCallback<FacilityListItem>() {
    override fun areItemsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem.facility.uuid == newItem.facility.uuid
    override fun areContentsTheSame(oldItem: FacilityListItem, newItem: FacilityListItem) = oldItem == newItem
  }

  object Builder {
    fun build(facility: Facility, searchQuery: String): FacilityListItem {
      val canHighlight = searchQuery.isNotBlank() && facility.name.contains(searchQuery, ignoreCase = true)

      val highlightedName = if (canHighlight) {
        Name.Highlighted(
            name = facility.name,
            highlightStart = facility.name.indexOf(searchQuery, ignoreCase = true),
            highlightEnd = facility.name.indexOf(searchQuery, ignoreCase = true) + searchQuery.length)
      } else {
        Name.Plain(facility.name)
      }

      val fullAddress = if (facility.streetAddress.isNullOrBlank()) {
        Address.WithoutStreet(district = facility.district, state = facility.state)
      } else {
        Address.WithStreet(street = facility.streetAddress!!, district = facility.district, state = facility.state)
      }

      return FacilityListItem(facility = facility, name = highlightedName, address = fullAddress)
    }
  }
}
