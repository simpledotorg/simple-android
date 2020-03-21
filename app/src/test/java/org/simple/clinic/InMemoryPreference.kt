package org.simple.clinic

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

data class InMemoryPreference<T>(
    private val key: String,
    private var defaultValue: T,
    private var actualValue: T? = null
): Preference<T> {

  private val valueSubject: Subject<T> = BehaviorSubject.createDefault(defaultValue)

  override fun isSet(): Boolean {
    return actualValue != null
  }

  override fun key(): String {
    return key
  }

  override fun asObservable(): Observable<T> {
    return valueSubject
  }

  override fun asConsumer(): Consumer<in T> {
    return Consumer { set(it) }
  }

  override fun defaultValue(): T {
    return defaultValue
  }

  override fun get(): T {
    return actualValue ?: defaultValue
  }

  override fun set(value: T) {
    actualValue = value
    valueSubject.onNext(value)
  }

  override fun delete() {
    actualValue = null
    valueSubject.onNext(defaultValue)
  }
}
