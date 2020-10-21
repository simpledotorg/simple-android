package org.simple.clinic.sync

import androidx.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.FacilitySyncGroupSwitchedAt
import org.simple.clinic.util.Optional
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class PurgeOnSync @Inject constructor(
    private val currentFacility: Provider<Facility>,
    private val appDatabase: AppDatabase,
    @TypedPreference(FacilitySyncGroupSwitchedAt) private val facilitySyncGroupSwitchedAt: Preference<Optional<Instant>>
) {

  @WorkerThread
  fun purgeUnusedData() {
    appDatabase.deletePatientsNotInFacilitySyncGroup(currentFacility.get())
  }
}
