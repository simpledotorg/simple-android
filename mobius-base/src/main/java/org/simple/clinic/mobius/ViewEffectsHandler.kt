package org.simple.clinic.mobius

interface ViewEffectsHandler<V> {
  fun handle(viewEffect: V)
}
