package org.simple.clinic.storage

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.instrumentation.InstrumentedPatientDao
import org.simple.clinic.instrumentation.TracingConfig
import org.simple.clinic.patient.Patient
import org.simple.clinic.util.scheduler.SchedulersProvider

@Module
class DaoModule {

  @AppScope
  @Provides
  fun providePatientDao(
      tracingConfig: TracingConfig,
      appDatabase: AppDatabase,
      schedulersProvider: SchedulersProvider
  ): Patient.RoomDao {
    return InstrumentedPatientDao(tracingConfig, appDatabase.patientDao(), schedulersProvider)
  }
}
