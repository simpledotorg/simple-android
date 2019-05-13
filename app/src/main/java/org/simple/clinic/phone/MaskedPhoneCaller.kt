package org.simple.clinic.phone

import io.reactivex.Completable

/**
 * See [OfflineMaskedPhoneCaller].
 */
interface MaskedPhoneCaller {
  fun maskedCall(numberToMask: String, caller: Caller): Completable

  fun normalCall(number: String, caller: Caller): Completable = Completable.complete()
}
