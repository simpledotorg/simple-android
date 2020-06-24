package org.simple.clinic.remoteconfig

import io.reactivex.Completable

class NoOpRemoteConfigService(private val configReader: ConfigReader) : RemoteConfigService {

  override fun reader(): ConfigReader {
    return configReader
  }

  override fun update(): Completable {
    return Completable.complete()
  }
}
