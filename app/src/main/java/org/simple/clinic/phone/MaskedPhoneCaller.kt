package org.simple.clinic.phone

import io.reactivex.Completable

/**
 * See [OfflineMaskedPhoneCaller].
 */
interface MaskedPhoneCaller {
  fun maskAndCall(numberToMask: String, caller: Caller): Completable
}
