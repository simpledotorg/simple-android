package org.simple.clinic.util

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import org.simple.clinic.navigation.v2.ScreenResult

fun Fragment.setFragmentResultListener(
    vararg requestKeys: Parcelable,
    listener: (requestKey: Parcelable, result: ScreenResult) -> Unit
) {
  requestKeys.forEach { requestKey ->
    val requestKeyName = requestKey::class.java.name
    setFragmentResultListener(requestKeyName, requestKeys, listener)
  }
}

private fun Fragment.setFragmentResultListener(
    requestKeyName: String,
    requestKeys: Array<out Parcelable>,
    listener: (requestKey: Parcelable, result: ScreenResult) -> Unit
) {
  setFragmentResultListener(requestKeyName) { requestKey, bundle ->
    val key = requestKeys.firstOrNull { key -> key::class.java.name == requestKey }

    if (key != null)
      listener(key, bundle.getParcelable(requestKey)!!)
    else
      throw IllegalArgumentException("Unknown request key: $key, in fragment: $tag")
  }
}

fun FragmentManager.setFragmentResult(requestKey: Parcelable, result: ScreenResult) {
  val requestKeyName = requestKey::class.java.name

  setFragmentResult(requestKeyName, bundleOf(
      requestKeyName to result
  ))
}
