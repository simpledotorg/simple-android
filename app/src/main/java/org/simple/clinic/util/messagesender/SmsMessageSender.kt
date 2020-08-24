package org.simple.clinic.util.messagesender

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class SmsMessageSender @Inject constructor(
    private val activity: AppCompatActivity
) : MessageSender {

  override fun send(phoneNumber: String, message: String) {
    val uri = Uri.parse("smsto:$phoneNumber")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
      putExtra("sms_body", message)
    }
    activity.startActivity(intent)
  }
}
