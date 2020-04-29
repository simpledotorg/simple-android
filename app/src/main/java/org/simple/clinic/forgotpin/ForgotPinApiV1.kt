package org.simple.clinic.forgotpin

import io.reactivex.Single

interface ForgotPinApiV1 {

  fun resetPin(request: ResetPinRequest): Single<ForgotPinResponse>
}
