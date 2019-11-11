package org.simple.clinic.sync

import org.simple.clinic.protocol.SyncProtocolsOnLogin
import org.simple.clinic.sync.indicator.SyncIndicatorStatusCalculator
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SyncSetup @Inject constructor(
    private val dataSync: DataSync,
    private val syncScheduler: SyncScheduler,
    private val syncIndicatorStatusCalculator: SyncIndicatorStatusCalculator,
    private val syncProtocolsOnLogin: SyncProtocolsOnLogin,
    private val dataSyncOnApproval: IDataSyncOnApproval,
    private val schedulersProvider: SchedulersProvider
) {

  fun run() {
    syncProtocolsOnLogin.listen()
    dataSyncOnApproval.sync()
    syncScheduler.schedule().subscribe()
    syncIndicatorStatusCalculator.updateSyncResults()
    dataSync.syncTheWorld().subscribeOn(schedulersProvider.io()).subscribe()
  }
}
