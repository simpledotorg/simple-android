package org.simple.clinic.sync.indicator

import org.simple.clinic.util.ResolvedError

interface SyncIndicatorUi {
  fun updateState(syncState: SyncIndicatorState)
  fun showErrorDialog(errorType: ResolvedError)
}
