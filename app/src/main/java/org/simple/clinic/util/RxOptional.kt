package org.simple.clinic.util

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Deprecated(
    message = "Use `extractIfPresent()` instead",
    replaceWith = ReplaceWith(
        expression = "extractIfPresent()",
        imports = ["org.simple.clinic.util.extractIfPresent"]
    )
)
fun <T : Any> Observable<Optional<T>>.filterAndUnwrapJust(): Observable<T> {
  return extractIfPresent()
}

fun <T : Any> Observable<Optional<T>>.extractIfPresent(): Observable<T> {
  return filter(Optional<T>::isPresent)
      .map(Optional<T>::get)
}

@Deprecated(
    message = "Use `Optional.get()` instead",
    replaceWith = ReplaceWith(
        expression = "map { it.get() }",
        imports = ["org.simple.clinic.util.extractIfPresent"]
    )
)
fun <T : Any> Observable<Optional<T>>.unwrapJust(): Observable<T> {
  return map(Optional<T>::get)
}

@Deprecated(
    message = "Use `extractIfPresent()` instead",
    replaceWith = ReplaceWith(
        expression = "extractIfPresent()",
        imports = ["org.simple.clinic.util.extractIfPresent"]
    )
)
fun <T : Any> Single<Optional<T>>.filterAndUnwrapJust(): Maybe<T> {
  return extractIfPresent()
}

fun <T : Any> Single<Optional<T>>.extractIfPresent(): Maybe<T> {
  return filter(Optional<T>::isPresent)
      .map(Optional<T>::get)
}

fun <T> Observable<Optional<T>>.filterNotPresent(): Observable<Optional<T>> {
  return filter { !it.isPresent() }
}
