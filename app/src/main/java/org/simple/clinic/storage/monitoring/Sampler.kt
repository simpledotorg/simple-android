package org.simple.clinic.storage.monitoring

import kotlin.random.Random

/**
 * Class used to encapsulate random sampling of any kind
 * */
class Sampler(
    private val sampleRate: Float
) {

  init {
    if (sampleRate < 0.0 || sampleRate > 1.0) {
      throw IllegalArgumentException("Sample rate must be between 0.0 and 1.0! Found $sampleRate")
    }
  }

  val sample: Boolean
    get() = Random.nextFloat() < sampleRate
}
