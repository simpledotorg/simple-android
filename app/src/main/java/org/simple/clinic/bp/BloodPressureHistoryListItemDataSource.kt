package org.simple.clinic.bp

import androidx.paging.PositionalDataSource
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem

class BloodPressureHistoryListItemDataSource : PositionalDataSource<BloodPressureHistoryListItem>() {
  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<BloodPressureHistoryListItem>) {

  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<BloodPressureHistoryListItem>) {

  }
}
