package org.simple.clinic.phone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri

interface Dialer {

  fun call(context: Context, phoneNumber: String)

  object Manual : Dialer {
    override fun call(context: Context, phoneNumber: String) {
      val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
      context.startActivity(intent)
    }
  }

  @SuppressLint("MissingPermission")
  object Automatic : Dialer {
    override fun call(context: Context, phoneNumber: String) {
      val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
      context.startActivity(intent)
    }
  }
}
