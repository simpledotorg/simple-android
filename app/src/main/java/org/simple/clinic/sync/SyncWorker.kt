package org.simple.clinic.sync

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import javax.inject.Inject

class SyncWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    private const val NO_GROUP = "no_group_id"
    private const val SYNC_GROUP = "sync_group_id"

    fun createWorkDataForSyncConfig(syncConfig: SyncConfig): Data {
      return Data
          .Builder()
          .putString(SYNC_GROUP, syncConfig.syncGroup.name)
          .build()
    }

    private fun readSyncGroup(workerParams: WorkerParameters): String {
      return workerParams.inputData.getString(SYNC_GROUP) ?: NO_GROUP
    }
  }

  @Inject
  lateinit var dataSync: DataSync

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)
    val syncGroup = readSyncGroup(workerParams = workerParams)

    try {
      performSync(syncGroup)
    } catch (e: Exception) {
      // Individual syncs report their errors internally so we can just
      // ignore this caught error. This is a good place for future
      // improvements like attempting a backoff based retry.
    }

    return Result.success()
  }

  private fun performSync(syncGroup: String) {
    if (syncGroup == NO_GROUP) {
      dataSync.syncTheWorld()
    } else {
      dataSync.sync(SyncGroup.valueOf(syncGroup))
    }
  }
}
