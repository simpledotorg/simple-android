package org.simple.clinic.overdue

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.overdue.AppointmentCancelReason.Dead
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.MovedToPrivatePractitioner
import org.simple.clinic.overdue.AppointmentCancelReason.Other
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.AppointmentCancelReason.TransferredToAnotherPublicHospital
import org.simple.clinic.overdue.AppointmentCancelReason.RefusedToComeBack
import org.simple.clinic.overdue.AppointmentCancelReason.Unknown
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class AppointmentCancelReason : Parcelable {

  /**
   * A human-readable string that can be printed when, say a reason is logged to analytics.
   * Unlike enums, calling toString() on sealed classes will print their fully qualified name
   * (e.g., org.simple.clinic.overdue.AppointmentCancelReason$Moved@6aceb1a5), which is not
   * very readable.
   */
  abstract override fun toString(): String

  @Parcelize
  object PatientNotResponding : AppointmentCancelReason() {
    override fun toString() = "PatientNotResponding"
  }

  @Parcelize
  object InvalidPhoneNumber : AppointmentCancelReason() {
    override fun toString() = "InvalidPhoneNumber"
  }

  @Parcelize
  object TransferredToAnotherPublicHospital : AppointmentCancelReason() {
    override fun toString() = "TransferredToAnotherPublicHospital"
  }

  @Parcelize
  object MovedToPrivatePractitioner : AppointmentCancelReason() {
    override fun toString() = "MovedToPrivatePractitioner"
  }

  @Parcelize
  object RefusedToComeBack : AppointmentCancelReason() {
    override fun toString() = "RefusedToComeBack"
  }

  @Parcelize
  object Dead : AppointmentCancelReason() {
    override fun toString() = "Dead"
  }

  @Parcelize
  object Other : AppointmentCancelReason() {
    override fun toString() = "Other"
  }

  @Parcelize
  data class Unknown(val actualValue: String) : AppointmentCancelReason() {
    override fun toString() = "Unknown ($actualValue)"
  }

  object TypeAdapter : SafeEnumTypeAdapter<AppointmentCancelReason>(
      knownMappings = mapOf(
          PatientNotResponding to "not_responding",
          InvalidPhoneNumber to "invalid_phone_number",
          TransferredToAnotherPublicHospital to "public_hospital_transfer",
          MovedToPrivatePractitioner to "moved_to_private",
          RefusedToComeBack to "refused_to_come_back",
          Dead to "dead",
          Other to "other"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(value: String?): AppointmentCancelReason? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(reason: AppointmentCancelReason?): String? = TypeAdapter.fromEnum(reason)
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(value: String?): AppointmentCancelReason? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(reason: AppointmentCancelReason?): String? = TypeAdapter.fromEnum(reason)
  }

  companion object {
    fun values(): Set<AppointmentCancelReason> {
      return TypeAdapter.knownMappings.keys
    }

    @VisibleForTesting
    fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}

val AppointmentCancelReason.displayTextRes: Int
  get() = when (this) {
    Dead -> R.string.contactpatient_patient_died
    InvalidPhoneNumber -> R.string.contactpatient_invalid_phone_number
    MovedToPrivatePractitioner -> R.string.contactpatient_moved_to_private
    Other -> R.string.contactpatient_other_reason
    PatientNotResponding -> R.string.contactpatient_patient_is_not_responding
    TransferredToAnotherPublicHospital -> R.string.contactpatient_public_hospital_transfer
    RefusedToComeBack -> R.string.contactpatient_refused_to_come_back
    is Unknown -> R.string.contactpatient_unknown
  }
