package org.simple.clinic.phone

import io.reactivex.Completable

/**
 * See [OfflineMaskedPhoneCaller].
 */
interface MaskedPhoneCaller {
  fun normalCall(number: String, caller: Caller): Completable

  fun maskedCall(numberToMask: String, caller: Caller): Completable
}
