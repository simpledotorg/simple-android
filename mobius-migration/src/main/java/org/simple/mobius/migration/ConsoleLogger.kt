package org.simple.mobius.migration

import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next

class ConsoleLogger<M, E, F> : MobiusLoop.Logger<M, E, F> {
  override fun beforeInit(model: M) {
    println("beforeInit: $model")
  }

  override fun afterInit(model: M, result: First<M, F>) {
    println("afterInit: $model, $result")
  }

  override fun beforeUpdate(model: M, event: E) {
    println("beforeUpdate: $model, $event")
  }

  override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
    println("afterUpdate: $model, $event $result")
  }

  override fun exceptionDuringInit(model: M, exception: Throwable) {
    println("exceptionDuringInit: $model, $exception")
  }

  override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
    println("exceptionDuringUpdate: $model, $event, $exception")
  }
}
