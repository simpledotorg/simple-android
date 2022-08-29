package org.simple.clinic.util

import android.content.Intent
import android.os.Parcelable

fun <T : Parcelable> Intent.popWithParcelableResult(name: String): T? {
  val result = getParcelableExtra<T>(name)
  removeExtra(name)
  return result
}
