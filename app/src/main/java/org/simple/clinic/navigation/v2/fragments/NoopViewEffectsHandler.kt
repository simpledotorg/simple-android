package org.simple.clinic.navigation.v2.fragments

import org.simple.clinic.mobius.ViewEffectsHandler

class NoopViewEffectsHandler<V> : ViewEffectsHandler<V> {

  override fun handle(viewEffect: V) {
    // Noop
  }
}
