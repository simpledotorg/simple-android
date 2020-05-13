package org.simple.clinic.scheduleappointment

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class ScheduleAppointmentEffectHandler {

  fun build(): ObservableTransformer<ScheduleAppointmentEffect, ScheduleAppointmentEvent> {
    return RxMobius
        .subtypeEffectHandler<ScheduleAppointmentEffect, ScheduleAppointmentEvent>()
        .build()
  }
}
