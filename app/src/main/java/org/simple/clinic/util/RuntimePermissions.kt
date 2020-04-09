package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import javax.inject.Inject

enum class RuntimePermissionResult {
  GRANTED,
  DENIED
}

class RuntimePermissions @Inject constructor() {

  fun check(activity: Activity, permission: String): RuntimePermissionResult {
    val permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
    return if (permissionGranted) DENIED else GRANTED
  }

  fun request(activity: Activity, permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
  }
}
