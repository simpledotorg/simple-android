package org.simple.clinic.util

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject

class Vibrator @Inject constructor(private val vibrator: Vibrator) {

  @Suppress("DEPRECATION")
  fun vibrate(millis: Long) {
    if (Build.VERSION.SDK_INT >= 26) {
      vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
      vibrator.vibrate(millis)
    }
  }
}

