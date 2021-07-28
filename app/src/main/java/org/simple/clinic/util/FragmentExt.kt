package org.simple.clinic.util

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import org.simple.clinic.navigation.v2.ScreenResult

fun Fragment.setFragmentResultListener(
    requestKey: Parcelable,
    listener: (requestKey: Parcelable, result: ScreenResult) -> Unit
) {
  val requestKeyName = requestKey::class.java.name

  setFragmentResultListener(requestKeyName) { _, bundle ->
    listener(requestKey, bundle.getParcelable(requestKeyName)!!)
  }
}

fun FragmentManager.setFragmentResult(requestKey: Parcelable, result: ScreenResult) {
  val requestKeyName = requestKey::class.java.name

  setFragmentResult(requestKeyName, bundleOf(
      requestKeyName to result
  ))
}
