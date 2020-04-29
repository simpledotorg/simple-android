package org.simple.clinic.util

/**
 * Clamps this float value between a given maximum and minimum
 *
 * @return
 * - [min] if `this` < [min]
 * - [max] if `this` > [max]
 * - `this` otherwise
 **/
fun Float.clamp(min: Float, max: Float) = when {
  this > max -> max
  this < min -> min
  else -> this
}