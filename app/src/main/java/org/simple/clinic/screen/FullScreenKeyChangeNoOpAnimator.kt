package org.simple.clinic.screen

import android.view.View
import flow.Direction
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.KeyChangeAnimator
import javax.inject.Inject

class FullScreenKeyChangeNoOpAnimator @Inject constructor() : KeyChangeAnimator<FullScreenKey> {

  override fun animate(
      outgoingKey: FullScreenKey?,
      outgoingView: View?,
      incomingKey: FullScreenKey,
      incomingView: View,
      direction: Direction,
      onCompleteListener: () -> Unit
  ) {
    onCompleteListener()
  }
}
