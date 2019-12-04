package org.simple.clinic.phone

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class PhoneCaller @Inject constructor(
    private val config: PhoneNumberMaskerConfig,
    private val activity: AppCompatActivity
) {

  fun normalCall(number: String, dialer: Dialer): Completable {
    return Completable.fromAction {
      dialer.call(context = activity, phoneNumber = number)
    }
  }

  fun secureCall(numberToMask: String, dialer: Dialer): Completable {
    return Single.just(maskNumber(numberToMask))
        .flatMapCompletable { maskedNumber -> normalCall(maskedNumber, dialer) }
  }

  private fun maskNumber(numberToMask: String): String {
    val proxyNumber = config.proxyPhoneNumber

    val stopCharacter = "#"
    val dtmfTones = "$numberToMask$stopCharacter"
    return "$proxyNumber,$dtmfTones"
  }
}
