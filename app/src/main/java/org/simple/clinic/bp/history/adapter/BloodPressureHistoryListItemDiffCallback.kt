package org.simple.clinic.bp.history.adapter

import androidx.recyclerview.widget.DiffUtil

class BloodPressureHistoryListItemDiffCallback : DiffUtil.ItemCallback<BloodPressureHistoryListItem>() {

  override fun areItemsTheSame(oldItem: BloodPressureHistoryListItem, newItem: BloodPressureHistoryListItem): Boolean {
    return oldItem.measurement.uuid == newItem.measurement.uuid
  }

  override fun areContentsTheSame(oldItem: BloodPressureHistoryListItem, newItem: BloodPressureHistoryListItem): Boolean {
    return oldItem == newItem
  }
}
