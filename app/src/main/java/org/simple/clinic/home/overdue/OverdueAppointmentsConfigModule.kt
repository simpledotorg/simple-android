package org.simple.clinic.home.overdue

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
object OverdueAppointmentsConfigModule {

  @Provides
  fun overdueAppointmentsConfig(configReader: ConfigReader): OverdueAppointmentsConfig {
    return OverdueAppointmentsConfig.read(configReader = configReader)
  }
}
