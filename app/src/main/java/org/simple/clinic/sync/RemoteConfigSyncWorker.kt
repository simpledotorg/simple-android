package org.simple.clinic.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import org.simple.clinic.remoteconfig.RemoteConfigService
import javax.inject.Inject

class RemoteConfigSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

  companion object {
    const val REMOTE_CONFIG_SYNC_WORKER = "remote_config_sync_worker"

    fun createWorkRequest(): OneTimeWorkRequest {
      val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .setRequiresBatteryNotLow(true)
          .build()

      return OneTimeWorkRequest
          .Builder(RemoteConfigSyncWorker::class.java)
          .setConstraints(constraints)
          .build()
    }
  }

  @Inject
  lateinit var remoteConfigService: RemoteConfigService

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)

    try {
      remoteConfigService.update()
    } catch (e: Exception) {
      // Individual syncs report their errors internally so we can just
      // ignore this caught error. This is a good place for future
      // improvements like attempting a backoff based retry.
    }

    return Result.success()
  }
}
