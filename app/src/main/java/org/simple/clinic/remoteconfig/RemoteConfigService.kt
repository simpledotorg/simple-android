package org.simple.clinic.remoteconfig

import io.reactivex.Completable

interface RemoteConfigService {

  fun reader(): ConfigReader

  fun update(): Completable
}
