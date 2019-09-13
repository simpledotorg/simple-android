package org.simple.mobius.migration

import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next

class NoopLogger<M, E, F> : MobiusLoop.Logger<M, E, F> {
  override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
    /* no-op */
  }

  override fun afterInit(model: M, result: First<M, F>) {
    /* no-op */
  }

  override fun beforeInit(model: M) {
    /* no-op */
  }

  override fun beforeUpdate(model: M, event: E) {
    /* no-op */
  }

  override fun exceptionDuringInit(model: M, exception: Throwable) {
    /* no-op */
  }

  override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
    /* no-op */
  }
}