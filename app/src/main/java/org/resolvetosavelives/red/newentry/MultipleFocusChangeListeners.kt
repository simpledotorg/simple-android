package org.resolvetosavelives.red.newentry

import io.reactivex.Observable

interface MultipleFocusChangeListeners {

  val focusChanges: Observable<Boolean>
}
