package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import javax.inject.Inject

enum class RuntimePermissionResult {
  GRANTED,
  DENIED,
  NEVER_ASK_AGAIN
}

class RuntimePermissions @Inject constructor() {

  fun check(activity: Activity, permission: String): RuntimePermissionResult {
    return if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
        NEVER_ASK_AGAIN
      } else {
        DENIED
      }
    } else {
      GRANTED
    }
  }

  fun request(activity: Activity, permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
  }
}
