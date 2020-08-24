package org.simple.clinic.util.messagesender

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder
import javax.inject.Inject

class WhatsAppMessageSender @Inject constructor(
    private val activity: AppCompatActivity
) : MessageSender {

  override fun send(phoneNumber: String, message: String) {
    val whatsAppPhoneNumber = phoneNumber.filter { it.isDigit() }
    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val intent = Intent().apply {
      action = Intent.ACTION_VIEW
      data = Uri.parse("https://wa.me/$whatsAppPhoneNumber?text=$encodedMessage")
    }
    activity.startActivity(intent)
  }
}
