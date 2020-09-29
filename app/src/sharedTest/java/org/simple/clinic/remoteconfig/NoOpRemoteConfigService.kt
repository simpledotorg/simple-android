package org.simple.clinic.remoteconfig

class NoOpRemoteConfigService(private val configReader: ConfigReader) : RemoteConfigService {

  override fun reader(): ConfigReader {
    return configReader
  }

  override fun update() {
    /* Nothing to do here */
  }
}
