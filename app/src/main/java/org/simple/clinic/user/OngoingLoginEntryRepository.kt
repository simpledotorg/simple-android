package org.simple.clinic.user

import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class OngoingLoginEntryRepository @Inject constructor(
    private val dao: OngoingLoginEntry.RoomDao
) {

  fun saveLoginEntry(ongoingLoginEntry: OngoingLoginEntry): Completable {
    return Completable.fromAction {
      dao.save(ongoingLoginEntry)
    }
  }

  fun entry(): Single<OngoingLoginEntry> {
    return dao.getEntry()
        .firstOrError()
        .map { entry ->
          if (entry.isEmpty()) throw IllegalStateException("User not present") else entry.first()
        }

  }

  fun entryImmediate(): OngoingLoginEntry {
    return dao.getEntryImmediate()!!
  }

  fun clearLoginEntry() {
    dao.delete()
  }
}
