package org.simple.clinic.phone

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class PhoneCaller @Inject constructor(
    private val activity: AppCompatActivity
) {

  fun normalCall(
      number: String,
      dialer: Dialer
  ) {
    dialer.call(context = activity, phoneNumber = number)
  }

  fun secureCall(
      visibleNumber: String,
      hiddenNumber: String,
      dialer: Dialer
  ) {
    val maskedNumber = maskNumber(visibleNumber = visibleNumber, hiddenNumber = hiddenNumber)
    normalCall(maskedNumber, dialer)
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
