package org.simple.clinic.overdue

import io.reactivex.Observable

class FollowUpScheduleRepository {

  fun schedules(): Observable<FollowUpSchedule> {
    return Observable.empty()
  }
}
