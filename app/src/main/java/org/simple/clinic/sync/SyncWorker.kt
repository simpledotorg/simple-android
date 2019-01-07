package org.simple.clinic.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import javax.inject.Inject

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    const val TAG = "patient-sync"
  }

  @Inject
  lateinit var dataSync: DataSync

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)
    dataSync.syncIfUserIsApproved().blockingAwait()
    return Result.SUCCESS
  }
}
