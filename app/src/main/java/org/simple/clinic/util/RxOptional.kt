package org.simple.clinic.util

import io.reactivex.Observable

fun <T : Any> Observable<Optional<T>>.filterAndUnwrapJust(): Observable<T> {
  return filter { it is Just }
      .map { (value) -> value!! }
}

fun <T : Any> Observable<Optional<T>>.unwrapJust(): Observable<T> {
  return map { it as Just }
      .map { (value) -> value }
}
