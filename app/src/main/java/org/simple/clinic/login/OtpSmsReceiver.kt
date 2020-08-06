package org.simple.clinic.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.simple.clinic.ClinicApp
import org.simple.clinic.LOGIN_OTP_LENGTH
import org.simple.clinic.R
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import timber.log.Timber
import javax.inject.Inject

// I don't like magic numbers, but I am not sure of the best
// place to put this since the message is hard coded.
//
// Message format (Next two lines):
// <#> 000000 is your Simple Verification Code
// 1zXflK9uq42
private const val OTP_START_INDEX = 4

class OtpSmsReceiver : BroadcastReceiver() {

  @Inject
  lateinit var userSession: UserSession

  // This is intentionally lazy so that the country specific retrofit endpoint
  // is not instantiated until the SMS is received.
  @Inject
  lateinit var loginUserWithOtp: dagger.Lazy<LoginUserWithOtp>

  // This is intentionally lazy so that the country specific retrofit endpoint
  // is not instantiated until the SMS is received.
  @Inject
  lateinit var dataSync: dagger.Lazy<DataSync>

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  init {
    ClinicApp.appComponent.inject(this)
  }

  override fun onReceive(context: Context, intent: Intent) {
    val extras: Bundle = intent.extras!!
    val status = extras[SmsRetriever.EXTRA_STATUS] as Status

    when (status.statusCode) {
      CommonStatusCodes.SUCCESS -> {
        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String

        val otp = message.substring(OTP_START_INDEX, OTP_START_INDEX + LOGIN_OTP_LENGTH)

        userSession.ongoingLoginEntry()
            .flatMap { entry -> loginUserWithOtp.get().loginWithOtp(entry.phoneNumber!!, entry.pin!!, otp) }
            .doOnSuccess(::onLoginResult)
            .subscribeOn(schedulersProvider.io())
            .observeOn(schedulersProvider.ui())
            .subscribe({
              when (it) {
                is LoginResult.UnexpectedError -> showToast(context, R.string.api_unexpected_error)
                is LoginResult.NetworkError -> showToast(context, R.string.api_network_error)
                is LoginResult.ServerError -> showToast(context, it.error)
                // No need to handle success case because it is handled in the home screen UI
              }
            }, {
              Timber.e(it, "Could not login with OTP!")
              showToast(context, R.string.api_unexpected_error)
            })
      }
    }
  }

  private fun onLoginResult(loginResult: LoginResult) {
    if (loginResult is LoginResult.Success) {
      userSession.clearOngoingLoginEntry()
      dataSync.get().fireAndForgetSync()
    }
  }

  private fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
  }

  private fun showToast(context: Context, @StringRes msgResId: Int) {
    Toast.makeText(context, msgResId, Toast.LENGTH_LONG).show()
  }
}
