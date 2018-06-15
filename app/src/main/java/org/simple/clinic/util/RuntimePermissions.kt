package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

enum class RuntimePermissionResult {
  GRANTED,
  DENIED,
  NEVER_ASK_AGAIN;
}

class RuntimePermissions {

  companion object {
    fun check(activity: Activity, permission: String): RuntimePermissionResult {
      if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
          return RuntimePermissionResult.NEVER_ASK_AGAIN
        }
        return RuntimePermissionResult.DENIED
      }
      return RuntimePermissionResult.GRANTED
    }
  }
}
