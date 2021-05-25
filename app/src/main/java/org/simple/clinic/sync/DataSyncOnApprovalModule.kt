package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

@Module
open class DataSyncOnApprovalModule {

  @Provides
  open fun bindSyncDataOnApproval(
      userSession: UserSession,
      dataSync: DataSync,
      schedulersProvider: SchedulersProvider
  ): IDataSyncOnApproval {
    return SyncDataOnApproval(userSession, dataSync, schedulersProvider)
  }
}
