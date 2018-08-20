package org.simple.clinic.login

import android.app.Application
import com.google.android.gms.auth.api.phone.SmsRetriever
import io.reactivex.Completable
import javax.inject.Inject

class LoginSmsListenerImpl @Inject constructor(private val application: Application) : LoginSmsListener {

  override fun startListeningForLoginSms(): Completable {
    return Completable.create { emitter ->
      val client = SmsRetriever.getClient(application)

      val task = client.startSmsRetriever()

      task.addOnSuccessListener { emitter.onComplete() }
      task.addOnFailureListener { emitter.onError(it) }
    }
  }
}
