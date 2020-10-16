package org.simple.clinic.sync

import androidx.annotation.WorkerThread
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility
import javax.inject.Inject
import javax.inject.Provider

class PurgeOnSync @Inject constructor(
    private val currentFacility: Provider<Facility>,
    private val appDatabase: AppDatabase
) {

  @WorkerThread
  fun purgeUnusedData() {
    appDatabase.deletePatientsNotInFacilitySyncGroup(currentFacility.get())
  }
}
