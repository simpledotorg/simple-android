package org.simple.clinic.util

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType

fun <T : Any> Observable<Optional<T>>.filterAndUnwrapJust(): Observable<T> {
  return ofType<Just<T>>()
      .map { (value) -> value }
}

fun <T : Any> Observable<Optional<T>>.unwrapJust(): Observable<T> {
  return map { it as Just }
      .map { (value) -> value }
}
