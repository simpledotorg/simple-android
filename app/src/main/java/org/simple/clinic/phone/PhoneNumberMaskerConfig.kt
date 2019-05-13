package org.simple.clinic.phone

import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.remoteconfig.ConfigReader

data class PhoneNumberMaskerConfig(
    val maskingEnabled: Boolean,
    val proxyPhoneNumber: String,
    val showPhoneMaskBottomSheet: Boolean = false
) {

  companion object {

    fun read(reader: ConfigReader): Observable<PhoneNumberMaskerConfig> {
      return Observable.fromCallable {
        PhoneNumberMaskerConfig(
            maskingEnabled = reader.boolean("phonenumbermasker_masking_enabled", default = false),
            proxyPhoneNumber = reader.string("phonenumbermasker_proxy_phone_number", default = BuildConfig.MASKED_PHONE_NUMBER))
      }
    }
  }
}
