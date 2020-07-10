package org.simple.clinic.user.clearpatientdata

import dagger.Module
import dagger.Provides
import java.time.Duration
import javax.inject.Named


@Module
class ClearPatientDataModule {

  @Provides
  @Named("clear_patient_data_sync_retry_count")
  fun provideSyncRetryCount(): Int = 1

  @Provides
  @Named("clear_patient_data_sync_timeout")
  fun provideSyncTimeout(): Duration = Duration.ofSeconds(15)
}
