package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService

@Module
class TestRemoteConfigModule {

  @Provides
  fun remoteConfigService(configReader: ConfigReader): RemoteConfigService {
    return NoOpRemoteConfigService(configReader)
  }

  @Provides
  fun remoteConfigReader(): ConfigReader {
    return DefaultValueConfigReader()
  }

  class DefaultValueConfigReader : ConfigReader {

    override fun string(name: String, default: String): String = default

    override fun boolean(name: String, default: Boolean): Boolean = default

    override fun double(name: String, default: Double): Double = default

    override fun long(name: String, default: Long): Long = default
  }

  class NoOpRemoteConfigService(private val configReader: ConfigReader) : RemoteConfigService {

    override fun reader(): ConfigReader {
      return configReader
    }

    override fun update(): Completable {
      return Completable.complete()
    }
  }
}
