package org.simple.clinic.phone

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject

class OfflineMaskedPhoneCaller @Inject constructor(
    private val configProvider: Single<PhoneNumberMaskerConfig>,
    private val activity: TheActivity
) : MaskedPhoneCaller {

  override fun maskAndCall(numberToMask: String, caller: Caller): Completable {
    return configProvider
        .map { config -> maskNumber(config, numberToMask) }
        .flatMapCompletable { maskedNumber -> callNumber(maskedNumber, caller) }
  }

  private fun maskNumber(config: PhoneNumberMaskerConfig, numberToMask: String): String {
    return if (config.maskingEnabled) {
      val proxyNumber = config.proxyPhoneNumber

      val stopCharacter = "#"
      val dtmfTones = "$numberToMask$stopCharacter"
      "$proxyNumber,$dtmfTones"

    } else {
      numberToMask
    }
  }

  private fun callNumber(maskedNumber: String, caller: Caller): Completable {
    return Completable.fromAction {
      caller.call(context = activity, phoneNumber = maskedNumber)
    }
  }
}
