package org.simple.clinic.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.KeyEvent
import io.reactivex.Observable
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.router.screen.ActivityResult
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

inline fun Dialog.overrideCancellation(crossinline backPressed: () -> Unit) {
  setCancelable(false)
  setCanceledOnTouchOutside(false)
  setOnKeyListener { _, keyCode, event ->
    if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
      backPressed()
      true
    } else {
      false
    }
  }
}
