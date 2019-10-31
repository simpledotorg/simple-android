package org.simple.clinic.phone

import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader

data class PhoneNumberMaskerConfig(
    val proxyPhoneNumber: String,
    val phoneMaskingFeatureEnabled: Boolean
) {

  companion object {

    fun read(reader: ConfigReader): Observable<PhoneNumberMaskerConfig> {
      return Observable.fromCallable {
        PhoneNumberMaskerConfig(
            proxyPhoneNumber = reader.string("phonenumbermasker_proxy_phone_number", default = ""),
            phoneMaskingFeatureEnabled = reader.boolean("phonenumbermasker_masking_enabled", default = false))
      }
    }
  }
}
