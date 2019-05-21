package org.simple.clinic.phone

import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.remoteconfig.ConfigReader

data class PhoneNumberMaskerConfig(
    val proxyPhoneNumber: String
) {

  companion object {

    fun read(reader: ConfigReader): Observable<PhoneNumberMaskerConfig> {
      return Observable.fromCallable {
        PhoneNumberMaskerConfig(
            proxyPhoneNumber = reader.string("phonenumbermasker_proxy_phone_number", default = BuildConfig.MASKED_PHONE_NUMBER))
      }
    }
  }
}
