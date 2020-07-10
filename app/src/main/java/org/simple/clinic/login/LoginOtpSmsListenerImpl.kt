package org.simple.clinic.login

import android.app.Application
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.simple.clinic.platform.crash.CrashReporter
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoginOtpSmsListenerImpl @Inject constructor(
    private val application: Application,
    private val crashReporter: CrashReporter
) : LoginOtpSmsListener {

  override fun listenForLoginOtp() {
    SmsRetriever
        .getClient(application)
        .startSmsRetriever()
        .await(Duration.ofSeconds(2)) { error ->
          // We don't care about any failures here since we have an option
          // to manually enter the OTP. We'll just report this to the crash
          // tracking tool.
          crashReporter.report(error)
        }
  }
}

private fun Task<*>.await(
    duration: Duration,
    exceptionHandler: (Throwable) -> Unit
) {
  try {
    Tasks.await(this, duration.toMillis(), TimeUnit.MILLISECONDS)
  } catch (e: Throwable) {
    exceptionHandler.invoke(e)
  }
}
