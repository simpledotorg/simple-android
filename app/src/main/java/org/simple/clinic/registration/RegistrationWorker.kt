package org.simple.clinic.registration

import androidx.work.Worker
import org.simple.clinic.ClinicApp
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class RegistrationWorker : Worker() {

  @Inject
  lateinit var api: RegistrationApiV1

  @Inject
  lateinit var userSession: UserSession

  override fun doWork(): WorkerResult {
    ClinicApp.appComponent.inject(this)

    return userSession.register()
        .map { result ->
          when (result) {
            is RegistrationResult.Success -> WorkerResult.SUCCESS
            is RegistrationResult.Error -> WorkerResult.RETRY
          }
        }
        .blockingGet()
  }
}
