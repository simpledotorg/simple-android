package org.simple.clinic.navigation.v2.fragments

import org.simple.clinic.mobius.ViewRenderer

class NoopViewRenderer<M> : ViewRenderer<M> {

  override fun render(model: M) {
    // Noop
  }
}
