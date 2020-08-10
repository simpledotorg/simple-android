package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class LoggedOutOfDeviceEffectHandler @Inject constructor(
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<LoggedOutOfDeviceEffect, LoggedOutOfDeviceEvent> = RxMobius
      .subtypeEffectHandler<LoggedOutOfDeviceEffect, LoggedOutOfDeviceEvent>()
      .build()
}
