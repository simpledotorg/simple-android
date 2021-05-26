package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Intent

data class ActivityResult(var requestCode: Int, var resultCode: Int, var data: Intent?) {

  fun succeeded(): Boolean {
    return resultCode == Activity.RESULT_OK
  }

  fun canceled(): Boolean {
    return resultCode == Activity.RESULT_CANCELED
  }
}
