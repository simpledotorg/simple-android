package org.simple.clinic.newentry

import io.reactivex.Observable

interface MultipleFocusChangeListeners {

  val focusChanges: Observable<Boolean>
}
