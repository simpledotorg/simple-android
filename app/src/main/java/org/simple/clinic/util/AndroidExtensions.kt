package org.simple.clinic.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import io.reactivex.Observable
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

fun Configuration.withLocale(overrideLocale: Locale): Configuration {
  // Configuration.getLocales is added after 24 and Configuration.locale is deprecated in 24
  if (Build.VERSION.SDK_INT >= 24) {
    if (!this.locales.isEmpty) {
      return this
    }
  } else {
    if (this.locale != null) {
      return this
    }
  }

  this.setLocale(overrideLocale)
  return this
}
