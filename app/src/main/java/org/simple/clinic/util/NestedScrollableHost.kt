/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simple.clinic.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.absoluteValue

/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the opposite direction as
 * ViewPager2 like RecyclerView, WebView, etc. The scrollable element needs to be the immediate and
 * only child of this host layout.
 *
 * Based on: https://github.com/android/views-widgets-samples/blob/87e58d1c6d0c832c5b362d33390148679182d314/ViewPager2/app/src/main/java/androidx/viewpager2/integration/testapp/NestedScrollableHost.kt
 */
class NestedScrollableHost(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

  private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

  private var initialX = 0f
  private var initialY = 0f

  override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
    handleInterceptTouchEvent(e)
    return super.onInterceptTouchEvent(e)
  }

  private fun handleInterceptTouchEvent(e: MotionEvent) {
    if (e.action == MotionEvent.ACTION_DOWN) {
      initialX = e.x
      initialY = e.y
      parent.requestDisallowInterceptTouchEvent(true)
    } else if (e.action == MotionEvent.ACTION_MOVE) {
      handleMoveEvents(e)
    }
  }

  private fun handleMoveEvents(motionEvent: MotionEvent) {
    val dx = motionEvent.x - initialX
    val dy = motionEvent.y - initialY

    // assuming ViewPager2 touch-slop is 2x touch-slop of child
    val scaledDx = dx.absoluteValue * .5f
    val scaledDy = dy.absoluteValue * 1f

    if (scaledDx < touchSlop && scaledDy < touchSlop) return

    if (scaledDy > scaledDx) {
      // Gesture is vertical, disallow parent touch events
      parent.requestDisallowInterceptTouchEvent(true)
    } else {
      // Gesture is horizontal, allow parent touch events
      parent.requestDisallowInterceptTouchEvent(false)
    }
  }
}
