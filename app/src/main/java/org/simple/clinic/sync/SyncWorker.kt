package org.simple.clinic.sync

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import javax.inject.Inject

class SyncWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    const val TAG = "patient-sync"
    private const val NO_GROUP_ID = "no-group-id"

    fun createWorkDataForSyncConfig(syncConfig: SyncConfig): Data {
      return Data
          .Builder()
          .putString("sync_group_id", syncConfig.syncGroupId.name)
          .build()
    }

    private fun readSyncGroupId(workerParams: WorkerParameters): String {
      return workerParams.inputData.getString("sync_group_id") ?: NO_GROUP_ID
    }
  }

  @Inject
  lateinit var dataSync: DataSync

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)
    val syncGroupId = readSyncGroupId(workerParams = workerParams)

    val completable = if (syncGroupId == NO_GROUP_ID) {
      dataSync.sync(null)

    } else {
      dataSync.sync(SyncGroup.valueOf(syncGroupId))
    }

    completable.blockingAwait()
    return Result.success()
  }
}
