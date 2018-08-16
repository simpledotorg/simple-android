package org.simple.clinic.sms

import io.reactivex.Observable

sealed class SmsReadResult {

  data class Success(val message: String) : SmsReadResult()

  data class Error(val cause: Throwable?) : SmsReadResult()
}

interface SmsReader {

  fun waitForSms(): Observable<SmsReadResult>
}
