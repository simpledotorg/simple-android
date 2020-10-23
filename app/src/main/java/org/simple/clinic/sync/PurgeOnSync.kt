package org.simple.clinic.sync

import androidx.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.FacilitySyncGroupSwitchedAt
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class PurgeOnSync(
    private val currentFacility: Provider<Facility>,
    private val appDatabase: AppDatabase,
    private val facilitySyncGroupSwitchedAt: Preference<Optional<Instant>>,
    private val delayPurgeAfterSwitchFor: Duration,
    private val clock: UtcClock
) {

  @Inject
  constructor(
      currentFacility: Provider<Facility>,
      appDatabase: AppDatabase,
      @TypedPreference(FacilitySyncGroupSwitchedAt) facilitySyncGroupSwitchedAt: Preference<Optional<Instant>>,
      clock: UtcClock,
      remoteConfig: ConfigReader
  ) : this(
      currentFacility = currentFacility,
      appDatabase = appDatabase,
      facilitySyncGroupSwitchedAt = facilitySyncGroupSwitchedAt,
      delayPurgeAfterSwitchFor = Duration.parse(remoteConfig.string("delay_purge_after_facility_sync_group_switch_duration", "P1D")),
      clock = clock
  )

  @WorkerThread
  fun purgeUnusedData() {
    val facilitySyncSwitchedAtInstant = facilitySyncGroupSwitchedAt.get()

    val shouldPurgeData = !facilitySyncSwitchedAtInstant.isPresent() || isSafeToPurgeData(facilitySyncSwitchedAtInstant.get())

    if(shouldPurgeData) appDatabase.deletePatientsNotInFacilitySyncGroup(currentFacility.get())
  }

  private fun isSafeToPurgeData(
      switchedAt: Instant
  ): Boolean {
    val now = Instant.now(clock)

    return Duration.between(switchedAt, now) > delayPurgeAfterSwitchFor
  }
}
