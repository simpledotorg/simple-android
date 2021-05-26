package org.simple.clinic.bp.history.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.NewBpButton

class BloodPressureHistoryListItemDiffCallback : DiffUtil.ItemCallback<BloodPressureHistoryListItem>() {

  override fun areItemsTheSame(
      oldItem: BloodPressureHistoryListItem,
      newItem: BloodPressureHistoryListItem
  ): Boolean {
    return when {
      oldItem is NewBpButton && newItem is NewBpButton -> true
      oldItem is BloodPressureHistoryItem && newItem is BloodPressureHistoryItem -> oldItem.measurement.uuid == newItem.measurement.uuid
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(
      oldItem: BloodPressureHistoryListItem,
      newItem: BloodPressureHistoryListItem
  ): Boolean {
    return oldItem == newItem
  }
}
