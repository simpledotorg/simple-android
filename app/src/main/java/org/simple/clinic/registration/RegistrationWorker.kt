package org.simple.clinic.registration

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.simple.clinic.ClinicApp
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class RegistrationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  @Inject
  lateinit var api: RegistrationApiV1

  @Inject
  lateinit var userSession: UserSession

  override fun doWork(): Result {
    ClinicApp.appComponent.inject(this)

    return userSession.register()
        .map { result ->
          when (result) {
            is RegistrationResult.Success -> Result.SUCCESS
            is RegistrationResult.Error -> Result.RETRY
          }
        }
        .blockingGet()
  }
}
