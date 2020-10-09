package org.simple.clinic.appconfig

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.net.URI

@JsonClass(generateAdapter = true)
@Parcelize
data class Country(

    @Json(name = "country_code")
    val isoCountryCode: String,

    @Json(name = "endpoint")
    val endpoint: URI,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "isd_code")
    val isdCode: String
) : Parcelable {

  val alternativeIdentifierType: Identifier.IdentifierType?
    get() {
      return when (isoCountryCode) {
        BANGLADESH -> Identifier.IdentifierType.BangladeshNationalId
        ETHIOPIA -> Identifier.IdentifierType.EthiopiaMedicalRecordNumber
        else -> null
      }
    }

  val areWhatsAppRemindersSupported: Boolean
    get() = isoCountryCode == INDIA

  companion object {
    const val INDIA = "IN"
    const val BANGLADESH = "BD"
    const val ETHIOPIA = "ET"
  }
}
