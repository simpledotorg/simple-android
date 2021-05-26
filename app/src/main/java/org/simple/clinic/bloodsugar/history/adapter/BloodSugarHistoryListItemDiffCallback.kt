package org.simple.clinic.bloodsugar.history.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.BloodSugarHistoryItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.NewBloodSugarButton

class BloodSugarHistoryListItemDiffCallback : DiffUtil.ItemCallback<BloodSugarHistoryListItem>() {
  override fun areItemsTheSame(
      oldItem: BloodSugarHistoryListItem,
      newItem: BloodSugarHistoryListItem
  ): Boolean {
    return when {
      oldItem is NewBloodSugarButton && newItem is NewBloodSugarButton -> true
      oldItem is BloodSugarHistoryItem && newItem is BloodSugarHistoryItem -> oldItem.measurement.uuid == newItem.measurement.uuid
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(
      oldItem: BloodSugarHistoryListItem,
      newItem: BloodSugarHistoryListItem
  ): Boolean {
    return oldItem == newItem
  }
}
