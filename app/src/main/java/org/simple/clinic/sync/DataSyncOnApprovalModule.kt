package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.user.UserSession

@Module
open class DataSyncOnApprovalModule {
  
  @Provides
  open fun bindSyncDataOnApproval(
      userSession: UserSession,
      dataSync: DataSync
  ): IDataSyncOnApproval {
    return SyncDataOnApproval(userSession, dataSync)
  }
}
