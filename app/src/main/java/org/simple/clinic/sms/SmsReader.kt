package org.simple.clinic.sms

import io.reactivex.Single

sealed class SmsReadResult {

  data class Success(val string: String) : SmsReadResult()

  data class Error(val cause: Throwable?) : SmsReadResult()
}

interface SmsReader {

  fun waitForSms(): Single<SmsReadResult>
}
