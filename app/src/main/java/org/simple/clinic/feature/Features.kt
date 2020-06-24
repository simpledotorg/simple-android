package org.simple.clinic.feature

import androidx.annotation.VisibleForTesting
import org.simple.clinic.remoteconfig.RemoteConfigService
import javax.inject.Inject

class Features @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) constructor(
    remoteConfigService: RemoteConfigService,
    private val overrides: Map<Feature, Boolean> = emptyMap()
) {

  @Inject
  constructor(remoteConfigService: RemoteConfigService) : this(remoteConfigService, emptyMap())

  private val configReader = remoteConfigService.reader()

  fun isEnabled(feature: Feature): Boolean {
    return when {
      feature in overrides -> overrides.getValue(feature)
      feature.remoteConfigKey.isNotBlank() -> configReader.boolean(feature.remoteConfigKey, feature.enabledByDefault)
      else -> feature.enabledByDefault
    }
  }

  fun isDisabled(feature: Feature): Boolean = !isEnabled(feature)
}
