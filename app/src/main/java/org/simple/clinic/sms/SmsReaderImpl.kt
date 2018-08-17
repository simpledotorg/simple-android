package org.simple.clinic.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.ActivityLifecycle
import javax.inject.Inject

private val smsReceivedIntentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

private class SmsBroadcastReceiver(private val onComplete: () -> Unit) : BroadcastReceiver() {

  private val emitter = PublishSubject.create<SmsReadResult>()

  val smsReadResults = emitter as Observable<SmsReadResult>

  override fun onReceive(context: Context, intent: Intent) {
    val extras = intent.extras
    val status = extras[SmsRetriever.EXTRA_STATUS] as Status

    when (status.statusCode) {
      CommonStatusCodes.SUCCESS -> {
        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String
        emitter.onNext(SmsReadResult.Success(message))
      }
      else -> emitter.onNext(SmsReadResult.SmsReadError())
    }

    onComplete()
  }
}

class SmsReaderImpl @Inject constructor(private val activity: TheActivity) : SmsReader {

  private val client = SmsRetriever.getClient(activity)

  private val smsReceiver = SmsBroadcastReceiver { unregisterSmsReceiver() }

  private val emitter = PublishSubject.create<SmsReadResult>()

  /**
  The broadcast receiver will get unregistered in two cases
  1. When the activity is stopped, and
  2. WHen the SMS read task completes

  We use this flag to stop the receiver from getting unregistered twice
   */
  private var isRegistered = false

  override fun waitForSms(): Observable<SmsReadResult> {

    val task = client.startSmsRetriever()
    handleSmsResults(task)

    return emitter
  }

  private fun handleSmsResults(task: Task<Void>) {
    task.addOnSuccessListener(activity) {
      registerSmsReceiver()
    }

    task.addOnFailureListener(activity) {
      emitter.onNext(SmsReadResult.SmsReadError())
    }
  }

  private fun registerSmsReceiver() {
    unregisterSmsReceiver()
    activity.registerReceiver(smsReceiver, smsReceivedIntentFilter)
    smsReceiver.smsReadResults
        .takeUntil(activity.lifecycle.stream().ofType(ActivityLifecycle.Stopped::class.java))
        .doFinally { unregisterSmsReceiver() }
        .subscribe { emitter.onNext(it) }
    isRegistered = true
  }

  private fun unregisterSmsReceiver() {
    if (isRegistered) {
      activity.unregisterReceiver(smsReceiver)
      isRegistered = false
    }
  }
}
