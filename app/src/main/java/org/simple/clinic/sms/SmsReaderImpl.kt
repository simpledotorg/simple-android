package org.simple.clinic.sms

import io.reactivex.Single
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject

class SmsReaderImpl @Inject constructor(private val activity: TheActivity) : SmsReader {

  override fun waitForSms() = Single.just(SmsReadResult.Success("") as SmsReadResult)!!
}
