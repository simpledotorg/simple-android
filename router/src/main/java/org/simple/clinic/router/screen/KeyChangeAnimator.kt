package org.simple.clinic.router.screen

import android.view.View
import flow.Direction

interface KeyChangeAnimator<T : Any> {

  fun animate(
      outgoingKey: T?,
      outgoingView: View?,
      incomingKey: T,
      incomingView: View,
      direction: Direction,
      onCompleteListener: () -> Unit
  )
}
