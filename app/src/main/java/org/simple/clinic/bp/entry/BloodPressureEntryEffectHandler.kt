package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

object BloodPressureEntryEffectHandler {
  fun create(): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    return ObservableTransformer {
      Observable.never<BloodPressureEntryEvent>()
    }
  }
}
