package org.simple.clinic.bp.history.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem_Old.BloodPressureHistoryItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem_Old.NewBpButton

class BloodPressureHistoryListItemDiffCallback : DiffUtil.ItemCallback<BloodPressureHistoryListItem_Old>() {

  override fun areItemsTheSame(
      oldItem: BloodPressureHistoryListItem_Old,
      newItem: BloodPressureHistoryListItem_Old
  ): Boolean {
    return when {
      oldItem is NewBpButton && newItem is NewBpButton -> true
      oldItem is BloodPressureHistoryItem && newItem is BloodPressureHistoryItem -> oldItem.measurement.uuid == newItem.measurement.uuid
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(
      oldItem: BloodPressureHistoryListItem_Old,
      newItem: BloodPressureHistoryListItem_Old
  ): Boolean {
    return oldItem == newItem
  }
}
