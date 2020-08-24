package org.simple.clinic.user

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility

@Module
class SessionModule {

  @Provides
  fun currentUser(appDatabase: AppDatabase): User {
    return appDatabase.userDao().userImmediate()!!
  }

  @Provides
  fun currentFacility(appDatabase: AppDatabase): Facility {
    val user = appDatabase.userDao().userImmediate()!!
    return appDatabase.userDao().currentFacilityImmediate(user.uuid)!!
  }

  @Provides
  fun currentFacilityNotifications(appDatabase: AppDatabase): Observable<Facility> {
    return appDatabase
        .userDao()
        .currentFacility()
        .toObservable()
  }
}
