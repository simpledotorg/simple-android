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
    return dao.getEntry().firstOrError()
  }

  fun clearLoginEntry(): Completable {
    return Completable.fromAction {
      dao.delete()
    }
  }


}
