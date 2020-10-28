package org.simple.clinic.util

import android.content.Context
import android.content.Intent
import io.reactivex.Observable
import org.simple.clinic.router.screen.ActivityResult

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
