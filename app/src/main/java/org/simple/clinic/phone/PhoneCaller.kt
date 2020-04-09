package org.simple.clinic.phone

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class PhoneCaller @Inject constructor(
    private val activity: AppCompatActivity
) {

  fun normalCall(
      number: String,
      dialer: Dialer
  ): Completable {
    return Completable.fromAction {
      dialer.call(context = activity, phoneNumber = number)
    }
  }

  fun secureCall(
      visibleNumber: String,
      hiddenNumber: String,
      dialer: Dialer
  ): Completable {
    return Single.just(maskNumber(visibleNumber = visibleNumber, hiddenNumber = hiddenNumber))
        .flatMapCompletable { maskedNumber -> normalCall(maskedNumber, dialer) }
  }

  private fun maskNumber(
      visibleNumber: String,
      hiddenNumber: String
  ): String {
    val stopCharacter = "#"
    val dtmfTones = "$hiddenNumber$stopCharacter"
    return "$visibleNumber,$dtmfTones"
  }
}
