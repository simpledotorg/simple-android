package org.resolvetosavelives.red.sync

import androidx.work.Worker
import io.reactivex.Single
import org.resolvetosavelives.red.RedApp
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class PatientSyncWorker : Worker() {

  companion object {
    const val TAG = "patient-sync"
  }

  @Inject
  lateinit var patientSync: PatientSync

  override fun doWork(): WorkerResult {
    RedApp.appComponent.inject(this)

    return patientSync.sync()
        .andThen(Single.just(WorkerResult.SUCCESS))
        .onErrorReturn({ error ->
          when {
            isRecoverable(error) -> WorkerResult.RETRY
            else -> {
              Timber.e(error, "Couldn't sync patients")
              WorkerResult.FAILURE
            }
          }
        })
        .blockingGet()
  }

  private fun isRecoverable(error: Throwable): Boolean {
    return when (error) {
      is SocketTimeoutException -> true
      is UnknownHostException -> true
      else -> false
    }
  }
}
