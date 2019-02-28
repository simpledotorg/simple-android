package org.simple.clinic.overdue

import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.SafeEnumTypeAdapter

sealed class AppointmentCancelReason {

  /**
   * A human-readable string that can be printed when, say a reason is logged to analytics.
   * Unlike enums, calling toString() on sealed classes will print their fully qualified name
   * (e.g., org.simple.clinic.overdue.AppointmentCancelReason$Moved@6aceb1a5), which is not
   * very readable.
   */
  abstract override fun toString(): String

  object PatientNotResponding : AppointmentCancelReason() {
    override fun toString() = "PatientNotResponding"
  }

  object InvalidPhoneNumber : AppointmentCancelReason() {
    override fun toString() = "InvalidPhoneNumber"
  }

  object TransferredToAnotherPublicHospital : AppointmentCancelReason() {
    override fun toString() = "TransferredToAnotherPublicHospital"
  }

  object MovedToPrivatePractitioner : AppointmentCancelReason() {
    override fun toString() = "MovedToPrivatePractitioner"
  }

  object Dead : AppointmentCancelReason() {
    override fun toString() = "Dead"
  }

  object Other : AppointmentCancelReason() {
    override fun toString() = "Other"
  }

  data class Unknown(val actualValue: String) : AppointmentCancelReason() {
    override fun toString() = "Unknown ($actualValue)"
  }

  object TypeAdapter : SafeEnumTypeAdapter<AppointmentCancelReason>(
      knownMappings = mapOf(
          PatientNotResponding to "not_responding",
          InvalidPhoneNumber to "invalid_phone_number",
          TransferredToAnotherPublicHospital to "public_hospital_transfer",
          MovedToPrivatePractitioner to "moved_to_private",
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
