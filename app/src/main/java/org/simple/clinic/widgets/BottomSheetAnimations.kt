package org.simple.clinic.widgets

import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/*
 * These methods were extracted out of BottomSheetActivity because they are also used in
 * LinkIdWithPatientView, which is not a BottomSheetActivity, but needs to behave like one.
 **/

fun animateBottomSheetIn(
    backgroundView: View,
    contentContainer: View,
    startAction: () -> Unit = {},
    endAction: () -> Unit = {}
) {
  backgroundView.alpha = 0f
  backgroundView.animate()
      .alpha(1f)
      .setDuration(200)
      .setInterpolator(FastOutSlowInInterpolator())
      .withStartAction(startAction)
      .withEndAction(endAction)
      .start()

  contentContainer.executeOnNextMeasure {
    contentContainer.translationY = contentContainer.height.toFloat()

    contentContainer.animate()
        .translationY(0f)
        .setDuration(250)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
  }
}

fun animateBottomSheetOut(
    backgroundView: View,
    contentContainer: View,
    startAction: () -> Unit = {},
    endAction: () -> Unit = {}
) {
  contentContainer.animate()
      .translationY(contentContainer.height.toFloat())
      .setDuration(250)
      .setInterpolator(FastOutSlowInInterpolator())
      .start()

  backgroundView.animate()
      .alpha(0f)
      .setDuration(100)
      .setInterpolator(FastOutSlowInInterpolator())
      .withStartAction(startAction)
      .withEndAction(endAction)
      .start()
}
