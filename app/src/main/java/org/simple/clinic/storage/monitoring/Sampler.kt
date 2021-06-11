package org.simple.clinic.storage.monitoring

import kotlin.random.Random

/**
 * Class used to encapsulate random sampling of any kind
 * */
class Sampler(
    sampleRate: Float
) {

  private val verifiedSampleRate = if (sampleRate < 0.0 || sampleRate > 1.0) 0F else sampleRate

  val sample: Boolean
    get() = Random.nextFloat() < verifiedSampleRate
}
