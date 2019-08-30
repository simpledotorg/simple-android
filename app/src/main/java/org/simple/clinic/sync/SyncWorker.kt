package org.simple.clinic.sync

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import timber.log.Timber
import javax.inject.Inject

class SyncWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    private const val NO_GROUP = "no-group-id"
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
    Timber.tag("SyncWork").i("Sync from SyncWorker, ID: ${workerParams.id}")

    val completable = if (syncGroup == NO_GROUP) {
      dataSync.sync(null)

    } else {
      dataSync.sync(SyncGroup.valueOf(syncGroup))
    }

    completable.blockingAwait()
    return Result.success()
  }
}
