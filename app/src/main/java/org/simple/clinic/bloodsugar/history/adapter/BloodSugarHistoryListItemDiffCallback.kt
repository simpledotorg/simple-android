package org.simple.clinic.bloodsugar.history.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryDeprecatedListItem.BloodSugarHistoryItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryDeprecatedListItem.NewBloodSugarButton

class BloodSugarHistoryListItemDiffCallback : DiffUtil.ItemCallback<BloodSugarHistoryDeprecatedListItem>() {
  override fun areItemsTheSame(
      oldItem: BloodSugarHistoryDeprecatedListItem,
      newItem: BloodSugarHistoryDeprecatedListItem
  ): Boolean {
    return when {
      oldItem is NewBloodSugarButton && newItem is NewBloodSugarButton -> true
      oldItem is BloodSugarHistoryItem && newItem is BloodSugarHistoryItem -> oldItem.measurement.uuid == newItem.measurement.uuid
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(
      oldItem: BloodSugarHistoryDeprecatedListItem,
      newItem: BloodSugarHistoryDeprecatedListItem
  ): Boolean {
    return oldItem == newItem
  }
}
