package org.simple.clinic.appconfig

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import org.threeten.extra.chrono.EthiopicChronology
import java.time.chrono.Chronology
import java.time.chrono.IsoChronology

@Parcelize
@JsonClass(generateAdapter = true)
data class Country(

    @Json(name = "country_code")
    val isoCountryCode: String,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "isd_code")
    val isdCode: String,

    @Json(name = "deployments")
    val deployments: List<Deployment>
) : Parcelable {

  val alternativeIdentifierType: Identifier.IdentifierType?
    get() {
      return when (isoCountryCode) {
        BANGLADESH -> Identifier.IdentifierType.BangladeshNationalId
        ETHIOPIA -> Identifier.IdentifierType.EthiopiaMedicalRecordNumber
        INDIA -> Identifier.IdentifierType.IndiaNationalHealthId
        SRI_LANKA -> Identifier.IdentifierType.SriLankaPersonalHealthNumber
        else -> null
      }
    }

  val areWhatsAppRemindersSupported: Boolean
    get() = isoCountryCode == INDIA

  val chronology: Chronology
    get() = when (isoCountryCode) {
      ETHIOPIA -> EthiopicChronology.INSTANCE
      else -> IsoChronology.INSTANCE
    }

  val fullDatePattern: String
    get() = when (isoCountryCode) {
      ETHIOPIA -> "dd/MM/yyyy"
      else -> "d-MMM-yyyy"
    }

  val exactDatePattern: String
    get() = when (isoCountryCode) {
      ETHIOPIA -> "dd/MM/yyyy"
      else -> "d MMMM, yyyy"
    }

  val fileDateTimePattern: String
    get() = when (isoCountryCode) {
      ETHIOPIA -> "dd MM yyyy h.mm.ss a"
      else -> "d MMM yyyy h.mm.ss a"
    }

  companion object {
    const val INDIA = "IN"
    const val BANGLADESH = "BD"
    const val ETHIOPIA = "ET"
    const val SRI_LANKA = "LK"
  }
}
