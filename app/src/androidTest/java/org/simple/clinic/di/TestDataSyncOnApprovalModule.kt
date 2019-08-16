package org.simple.clinic.di

import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.DataSyncOnApprovalModule
import org.simple.clinic.sync.IDataSyncOnApproval
import org.simple.clinic.user.UserSession

class TestDataSyncOnApprovalModule : DataSyncOnApprovalModule() {
  override fun bindSyncDataOnApproval(userSession: UserSession, dataSync: DataSync): IDataSyncOnApproval {
    return object : IDataSyncOnApproval {
      override fun sync() {}
    }
  }
}
