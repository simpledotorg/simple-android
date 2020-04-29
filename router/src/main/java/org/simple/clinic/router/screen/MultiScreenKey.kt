package org.simple.clinic.router.screen

import android.os.Parcelable

import java.util.ArrayList

import flow.MultiKey

/**
 * Implemented by [FullScreenKeys][FullScreenKey] that have nested screens.
 */
interface MultiScreenKey : MultiKey {

  /**
   * Could be [MultiScreenKey] for nested children.
   */
  fun children(): List<Parcelable>

  override fun getKeys(): List<Any> {
    return ArrayList(children())
  }
}
