package org.simple.clinic.navigation.v2

import android.os.Parcelable

interface ExpectsResult {
  fun onScreenResult(requestType: Parcelable, result: ScreenResult)
}
