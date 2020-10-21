package org.simple.clinic.sync

import androidx.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.FacilitySyncGroupSwitchedAt
import org.simple.clinic.util.Optional
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class PurgeOnSync(
    private val currentFacility: Provider<Facility>,
    private val appDatabase: AppDatabase,
    private val facilitySyncGroupSwitchedAt: Preference<Optional<Instant>>,
    private val delayPurgeAfterSwitchFor: Duration
) {

  @Inject
  constructor(
      currentFacility: Provider<Facility>,
      appDatabase: AppDatabase,
      @TypedPreference(FacilitySyncGroupSwitchedAt) facilitySyncGroupSwitchedAt: Preference<Optional<Instant>>
  ) : this(
      currentFacility = currentFacility,
      appDatabase = appDatabase,
      facilitySyncGroupSwitchedAt = facilitySyncGroupSwitchedAt,
      delayPurgeAfterSwitchFor = Duration.ofHours(24)
  )

  @WorkerThread
  fun purgeUnusedData() {
    appDatabase.deletePatientsNotInFacilitySyncGroup(currentFacility.get())
  }
}
