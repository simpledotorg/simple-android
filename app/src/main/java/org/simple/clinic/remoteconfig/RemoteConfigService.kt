package org.simple.clinic.remoteconfig

interface RemoteConfigService {

  fun reader(): ConfigReader

  fun update()
}
