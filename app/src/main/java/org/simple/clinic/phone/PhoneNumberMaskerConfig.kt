package org.simple.clinic.phone

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Observable
import org.simple.clinic.BuildConfig

data class PhoneNumberMaskerConfig(
    val maskingEnabled: Boolean,
    val proxyPhoneNumber: String
) {

  companion object {
    private const val KEY_MASKING_ENABLED = "phonenumbermasker_masking_enabled"
    private const val KEY_PROXY_PHONE_NUM = "phonenumbermasker_proxy_phone_number"

    fun read(source: FirebaseRemoteConfig): Observable<PhoneNumberMaskerConfig> {
      return Observable.fromCallable {
        PhoneNumberMaskerConfig(
            maskingEnabled = source.getBoolean(KEY_MASKING_ENABLED),
            proxyPhoneNumber = source.getString(KEY_PROXY_PHONE_NUM))
      }
    }

    fun defaultValues(): Map<String, Any> {
      return mapOf(
          KEY_MASKING_ENABLED to false,
          KEY_PROXY_PHONE_NUM to BuildConfig.MASKED_PHONE_NUMBER
      )
    }
  }
}
