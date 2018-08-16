package org.simple.clinic.sms

import io.reactivex.Observable

sealed class SmsReadResult {

  data class Success(val string: String) : SmsReadResult()

<<<<<<< HEAD
  data class Error(val cause: Throwable?) : SmsReadResult()
=======
  data class SmsReadError(val cause: Throwable? = null) : SmsReadResult()
>>>>>>> Add initial implementation of SmsReaderImpl
}

interface SmsReader {

  fun waitForSms(): Observable<SmsReadResult>
}
