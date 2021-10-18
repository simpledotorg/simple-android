package org.simple.clinic.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.ActivityResult
import java.util.Locale

inline fun Context.wrap(wrapper: (Context) -> Context): Context = wrapper(this)

inline fun <reified T> Observable<ActivityResult>.extractSuccessful(
    requestCode: Int,
    crossinline resultMapper: (Intent) -> T
): Observable<T> {
  return filter { it.requestCode == requestCode && it.succeeded() && it.data != null }
      .map { resultMapper(it.data!!) }
}

fun Observable<ActivityResult>.filterIfSuccessful(
    requestCode: Int
): Observable<ActivityResult> {
  return filter { it.requestCode == requestCode && it.succeeded() && it.data != null }
}

fun Intent.disableAnimations(): Intent {
  flags = flags or Intent.FLAG_ACTIVITY_NO_ANIMATION

  return this
}

fun Activity.disablePendingTransitions() {
  overridePendingTransition(0, 0)
}

fun Activity.finishWithoutAnimations() {
  overridePendingTransition(0, 0)
  finish()
}

fun Configuration.withLocale(
    overrideLocale: Locale,
    features: Features
): Configuration {
  val canOverrideLocale = features.isEnabled(Feature.ChangeLanguage) && !isLocaleAlreadyOverriden()

  if (canOverrideLocale) {
    this.setLocale(overrideLocale)
  }

  return this
}

private fun Configuration.isLocaleAlreadyOverriden(): Boolean {
  return when {
    Build.VERSION.SDK_INT >= 24 && !this.locales.isEmpty -> true
    Build.VERSION.SDK_INT < 24 && this.locale != null -> true
    else -> false
  }
}

inline fun Dialog.onBackPressed(crossinline backPressed: () -> Unit) {
  setOnKeyListener { _, keyCode, event ->
    if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
      backPressed()
      true
    } else {
      false
    }
  }
}

// Source: https://github.com/afollestad/material-dialogs/blob/66582d9993bcf55bfea873b4bf484a429ce3df36/core/src/main/java/com/afollestad/materialdialogs/utils/MDUtil.kt#L103
@ColorInt
fun Context.resolveColor(
    @ColorRes colorRes: Int? = null,
    @AttrRes attrRes: Int? = null,
    fallback: (() -> Int)? = null
): Int {
  if (attrRes != null) {
    val a = theme.obtainStyledAttributes(intArrayOf(attrRes))
    try {
      val result = a.getColor(0, 0)
      if (result == 0 && fallback != null) {
        return fallback()
      }
      return result
    } finally {
      a.recycle()
    }
  }
  return ContextCompat.getColor(this, colorRes ?: 0)
}

fun Context.resolveFloat(attrRes: Int, fallback: (() -> Float)? = null): Float {
  val a = theme.obtainStyledAttributes(intArrayOf(attrRes))
  try {
    val result = a.getFloat(0, 0f)
    if (result == 0f && fallback != null) {
      return fallback()
    }
    return result
  } finally {
    a.recycle()
  }
}
