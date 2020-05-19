package org.simple.clinic.sync.indicator

import org.simple.clinic.util.ResolvedError

interface SyncIndicatorUiActions {
  fun showErrorDialog(errorType: ResolvedError)
}
