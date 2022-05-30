package org.simple.sharedTestCode.remoteconfig

import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService

class NoOpRemoteConfigService(private val configReader: ConfigReader) : RemoteConfigService {

  override fun reader(): ConfigReader {
    return configReader
  }

  override fun update() {
    /* Nothing to do here */
  }
}
