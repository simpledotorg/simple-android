package org.simple.clinic.sync

import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.simple.clinic.sync.indicator.SyncIndicatorStatusCalculator
import javax.inject.Inject

class SyncSetup @Inject constructor(
    private val syncScheduler: SyncScheduler,
    private val syncIndicatorStatusCalculator: SyncIndicatorStatusCalculator,
    private val dataSyncOnApproval: IDataSyncOnApproval
) {

  @CheckReturnValue
  fun run(): Disposable {
    return CompositeDisposable().apply {
      addAll(
          dataSyncOnApproval.sync(),
          syncScheduler.schedule().subscribe(),
          syncIndicatorStatusCalculator.updateSyncResults()
      )
    }
  }
}
