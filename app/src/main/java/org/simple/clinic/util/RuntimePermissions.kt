package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

enum class RuntimePermissionResult {
  GRANTED,
  DENIED,
  NEVER_ASK_AGAIN
}

object RuntimePermissions {

  fun check(activity: Activity, permission: String): RuntimePermissionResult {
    return if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        RuntimePermissionResult.NEVER_ASK_AGAIN
      } else {
        RuntimePermissionResult.DENIED
      }
    } else {
      RuntimePermissionResult.GRANTED
    }
  }

  fun request(activity: Activity, permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
  }
}
