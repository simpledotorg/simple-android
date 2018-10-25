package org.simple.clinic.phone

import io.reactivex.Completable

/**
 * See [TwilioMaskedPhoneCaller].
 */
interface MaskedPhoneCaller {
  fun maskAndCall(numberToMask: String, caller: Caller): Completable
}
