package org.simple.clinic.forgotpin

import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

interface ForgotPinApiV1 {

  fun resetPin(request: ResetPinRequest): Completable {
    return Completable.complete()
        .delay(3L, TimeUnit.SECONDS)
  }

  fun resetPin2(request: ResetPinRequest): Single<ForgotPinResponse>
}
