package org.resolvetosavelives.red.sync

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.threeten.bp.Duration

@Module
class PatientSyncModule {

  @Provides
  fun patientSyncConfig(): Single<PatientSyncConfig> {
    // In the future, this may come from the server.
    return Single.just(PatientSyncConfig(Duration.ofHours(1)))
  }
}
