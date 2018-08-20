package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.threeten.bp.Instant
import javax.inject.Named

@Module
class PatientsModule {

  @Provides
  @Named("approval_status_changed_at")
  fun approvalStatusChangeTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Instant> {
    return rxSharedPrefs.getObject("approval_status_changed_at", Instant.EPOCH, InstantRxPreferencesConverter())
  }

  @Provides
  @Named("approved_status_dismissed")
  fun hasUserDismissedApprovedStatus(rxSharedPrefs: RxSharedPreferences): Preference<Boolean> {
    return rxSharedPrefs.getBoolean("approved_status_dismissed", false)
  }
}
