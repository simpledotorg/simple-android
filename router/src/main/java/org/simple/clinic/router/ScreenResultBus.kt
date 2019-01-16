package org.simple.clinic.router

import androidx.annotation.CheckResult

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ScreenResultBus {

  private val results = PublishSubject.create<Any>()

  fun send(result: Any) {
    results.onNext(result)
  }

  @CheckResult
  fun streamResults(): Observable<Any> {
    return results
  }
}
