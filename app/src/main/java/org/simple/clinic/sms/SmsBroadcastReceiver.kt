package org.simple.clinic.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.simple.clinic.ClinicApp
import timber.log.Timber

class SmsBroadcastReceiver : BroadcastReceiver() {

  val userSession = ClinicApp.appComponent.userSession()

  override fun onReceive(context: Context, intent: Intent) {
    val extras = intent.extras
    val status = extras[SmsRetriever.EXTRA_STATUS] as Status

    when (status.statusCode) {
      CommonStatusCodes.SUCCESS -> {
        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String

        // I don't like magic numbers, but I am not sure of the best place to put this since the message is hard coded
        val otp = message.substring(4, 8)

        // TODO: Remove this log once we actually schedule the call to
        Timber.d("Received OTP: $otp, User Session: $userSession")
      }
    }
  }
}
