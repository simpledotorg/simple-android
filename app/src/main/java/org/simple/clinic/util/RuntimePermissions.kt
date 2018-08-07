package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

enum class RuntimePermissionResult {
  GRANTED,
  DENIED,
  NEVER_ASK_AGAIN;
}

object RuntimePermissions {

  fun check(activity: Activity, permission: String): RuntimePermissionResult {
    if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        return RuntimePermissionResult.NEVER_ASK_AGAIN
      }
      return RuntimePermissionResult.DENIED
    }
    return RuntimePermissionResult.GRANTED
  }

  fun request(activity: Activity, permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
  }
}
