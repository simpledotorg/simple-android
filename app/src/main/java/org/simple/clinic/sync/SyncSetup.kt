package org.simple.clinic.sync

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.simple.clinic.protocol.SyncProtocolsOnLogin
import org.simple.clinic.sync.indicator.SyncIndicatorStatusCalculator
import javax.annotation.CheckReturnValue
import javax.inject.Inject

class SyncSetup @Inject constructor(
    private val syncScheduler: SyncScheduler,
    private val syncIndicatorStatusCalculator: SyncIndicatorStatusCalculator,
    private val syncProtocolsOnLogin: SyncProtocolsOnLogin,
    private val dataSyncOnApproval: IDataSyncOnApproval
) {

  @CheckReturnValue
  fun run(): Disposable {
    return CompositeDisposable().apply {
      addAll(
          syncProtocolsOnLogin.listen(),
          dataSyncOnApproval.sync(),
          syncScheduler.schedule().subscribe(),
          syncIndicatorStatusCalculator.updateSyncResults()
      )
    }
  }
}
