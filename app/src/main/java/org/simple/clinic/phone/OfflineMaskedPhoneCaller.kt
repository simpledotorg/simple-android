package org.simple.clinic.phone

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject

class OfflineMaskedPhoneCaller @Inject constructor(
    private val configProvider: Observable<PhoneNumberMaskerConfig>,
    private val activity: TheActivity
) : MaskedPhoneCaller {

  override fun normalCall(number: String, caller: Caller) =
      Completable.fromAction {
        caller.call(context = activity, phoneNumber = number)
      }

  override fun maskedCall(numberToMask: String, caller: Caller) =
      configProvider
          .map { config -> maskNumber(config, numberToMask) }
          .flatMapCompletable { maskedNumber -> normalCall(maskedNumber, caller) }

  private fun maskNumber(config: PhoneNumberMaskerConfig, numberToMask: String): String {
    val proxyNumber = config.proxyPhoneNumber

    val stopCharacter = "#"
    val dtmfTones = "$numberToMask$stopCharacter"
    return "$proxyNumber,$dtmfTones"
  }
}
