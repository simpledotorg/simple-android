package org.simple.clinic.patient.businessid

import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.EthiopiaMedicalRecordNumber
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.SriLankaNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.SriLankaPersonalHealthNumber
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.room.SafeEnumTypeAdapter

@Parcelize
data class Identifier(

    @ColumnInfo(name = "identifier")
    val value: String,

    @ColumnInfo(name = "identifierType")
    val type: IdentifierType
) : Parcelable {

  fun displayValue(): String {
    return try {
      when (type) {
        BpPassport -> {
          val shortCode = BpPassport.shortCode(this)

          val prefix = shortCode.substring(0, 3)
          val suffix = shortCode.substring(3)

          "$prefix${Unicode.nonBreakingSpace}$suffix"
        }

        BangladeshNationalId -> value
        EthiopiaMedicalRecordNumber -> value
        IndiaNationalHealthId -> {
          val enteredCode = value
          val prefix = enteredCode.substring(0, 2)
          val subString1 = enteredCode.substring(2, 6)
          val subString2 = enteredCode.substring(6, 10)
          val suffix = enteredCode.substring(10)

          "$prefix${Unicode.nonBreakingSpace}$subString1${Unicode.nonBreakingSpace}$subString2${Unicode.nonBreakingSpace}$suffix"
        }

        SriLankaNationalId -> value
        SriLankaPersonalHealthNumber -> value
        is Unknown -> value
      }
    } catch (e: StringIndexOutOfBoundsException) {
      // TODO (SM): Get more information on the crash and resolve the issue accordingly
      val errorMessage = "Failed to parse the `Identifier` of type: $type, with value length: ${value.length}"
      CrashReporter.report(StringIndexOutOfBoundsException(errorMessage))

      return value
    }
  }

  fun displayType(resources: Resources): String {
    return resources.getString(displayTypeResId())
  }

  @StringRes
  fun displayTypeResId(): Int {
    return when (type) {
      BpPassport -> R.string.identifiertype_bp_passport
      BangladeshNationalId -> R.string.identifiertype_bangladesh_national_id
      EthiopiaMedicalRecordNumber -> R.string.identifiertype_ethiopia_medical_record_number
      IndiaNationalHealthId -> R.string.identifiertype_india_national_health_id
      SriLankaNationalId -> R.string.identifiertype_sri_lanka_national_id
      SriLankaPersonalHealthNumber -> R.string.identifiertype_sri_lanka_personal_health_number
      is Unknown -> R.string.identifiertype_unknown
    }
  }

  sealed class IdentifierType : Parcelable {

    @Parcelize
    data object BpPassport : IdentifierType() {

      @IgnoredOnParcel
      const val SHORT_CODE_LENGTH = 7

      fun shortCode(identifier: Identifier): String {
        require(identifier.type == BpPassport) {
          "Required type to be [${TypeAdapter.fromEnum(BpPassport)}], but was [${TypeAdapter.fromEnum(identifier.type)}]"
        }

        return identifier.value.filter { it.isDigit() }.take(SHORT_CODE_LENGTH)
      }
    }

    @Parcelize
    data object BangladeshNationalId : IdentifierType()

    @Parcelize
    data object EthiopiaMedicalRecordNumber : IdentifierType()

    @Parcelize
    data object IndiaNationalHealthId : IdentifierType()

    @Parcelize
    data object SriLankaNationalId : IdentifierType()

    @Parcelize
    data object SriLankaPersonalHealthNumber : IdentifierType()

    @Parcelize
    data class Unknown(val actual: String) : IdentifierType()

    object TypeAdapter : SafeEnumTypeAdapter<IdentifierType>(
        knownMappings = mapOf(
            BpPassport to "simple_bp_passport",
            BangladeshNationalId to "bangladesh_national_id",
            EthiopiaMedicalRecordNumber to "ethiopia_medical_record",
            IndiaNationalHealthId to "india_national_health_id",
            SriLankaNationalId to "sri_lanka_national_id",
            SriLankaPersonalHealthNumber to "sri_lanka_personal_health_number"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }

    companion object {
      @VisibleForTesting(otherwise = VisibleForTesting.NONE)
      fun values() = TypeAdapter.knownMappings.keys.toList()
    }
  }
}
