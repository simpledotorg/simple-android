package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.util.Optional
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val dao: Facility.RoomDao,
    private val loggedInUser: Preference<Optional<LoggedInUser>>
) {

  fun currentFacility(): Observable<Facility> {
    val currentUserFacilityUuid = loggedInUser.get().toNullable()!!.facilityUuid
    return Observable.just(dao.getOne(currentUserFacilityUuid))
  }

  fun mergeWithLocalData(payloads: List<FacilityPayload>): Completable {
    return payloads
        .toObservable()
        .filter { payload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }
}
