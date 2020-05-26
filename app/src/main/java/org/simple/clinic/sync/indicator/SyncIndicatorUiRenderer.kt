package org.simple.clinic.sync.indicator

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending

class SyncIndicatorUiRenderer(private val ui: SyncIndicatorUi) : ViewRenderer<SyncIndicatorModel> {
  override fun render(model: SyncIndicatorModel) {
    if (model.lastSyncedState != null && model.lastSyncedState.isEmpty()) {
      ui.updateState(SyncPending)
    }

    if (model.syncIndicatorState != null) {
      ui.updateState(model.syncIndicatorState)
    }
  }
}
