package org.simple.clinic.selectcountry.adapter

import androidx.recyclerview.widget.DiffUtil

class SelectableCountryItemDiffCallback : DiffUtil.ItemCallback<SelectableCountryItem>() {

  override fun areItemsTheSame(
      oldItem: SelectableCountryItem,
      newItem: SelectableCountryItem
  ): Boolean {
    return oldItem.country.isdCode == newItem.country.isdCode
  }

  override fun areContentsTheSame(
      oldItem: SelectableCountryItem,
      newItem: SelectableCountryItem
  ): Boolean {
    return oldItem == newItem
  }
}
