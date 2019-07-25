package org.simple.clinic.login

import android.app.Application
import com.google.android.gms.auth.api.phone.SmsRetriever
import io.reactivex.Completable
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class LoginOtpSmsListenerImpl @Inject constructor(
    private val application: Application,
    private val schedulersProvider: SchedulersProvider
) : LoginOtpSmsListener {

  override fun listenForLoginOtp(): Completable {
    return Completable
        .create { emitter ->
          val client = SmsRetriever.getClient(application)

          val task = client.startSmsRetriever()

          task.addOnSuccessListener { emitter.onComplete() }
          task.addOnFailureListener { emitter.onError(it) }
        }
        .observeOn(schedulersProvider.io())
  }
}
