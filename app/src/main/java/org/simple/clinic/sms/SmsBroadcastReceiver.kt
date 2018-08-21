package org.simple.clinic.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.simple.clinic.ClinicApp
import timber.log.Timber

private const val OTP_START_INDEX = 4
private const val OTP_LENGTH = 6

class SmsBroadcastReceiver : BroadcastReceiver() {

  private val userSession = ClinicApp.appComponent.userSession()

  override fun onReceive(context: Context, intent: Intent) {
    val extras = intent.extras
    val status = extras[SmsRetriever.EXTRA_STATUS] as Status

    when (status.statusCode) {
      CommonStatusCodes.SUCCESS -> {
        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String

        // I don't like magic numbers, but I am not sure of the best place to put this since the message is hard coded
        val otp = message.substring(OTP_START_INDEX, OTP_START_INDEX + OTP_LENGTH)

        // TODO: Remove this log once we actually schedule the call to
        Timber.d("Received OTP: $otp, User Session: $userSession")
      }
    }
  }
}
