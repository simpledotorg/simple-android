package org.simple.clinic.overdue

import androidx.room.TypeConverter
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

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

  @Deprecated(message = "Removed in V2 API")
  object Moved : AppointmentCancelReason() {
    override fun toString() = "Moved"
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

  /**
   * It'll be nice to write a code generator instead of hand-typing
   * and manually maintaining this adapter in the future.
   */
  object TypeAdapter {

    val KNOWN_MAPPINGS = mapOf(
        PatientNotResponding to "not_responding",
        Moved to "moved",
        InvalidPhoneNumber to "invalid_phone_number",
        TransferredToAnotherPublicHospital to "public_hospital_transfer",
        MovedToPrivatePractitioner to "moved_to_private",
        Dead to "dead",
        Other to "other")

    fun toEnum(reason: String?): AppointmentCancelReason? {
      if (reason == null) {
        return null
      }

      val foundEnum = KNOWN_MAPPINGS.entries
          .find { (_, jsonKey) -> jsonKey == reason }
          ?.key
      return foundEnum ?: Unknown(actualValue = reason)
    }

    fun fromEnum(reason: AppointmentCancelReason?): String? {
      if (reason == null) {
        return null
      }

      return if (reason is Unknown) {
        reason.actualValue
      } else {
        KNOWN_MAPPINGS[reason] ?: throw AssertionError("Unknown reason enum: $reason")
      }
    }
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(reason: String?): AppointmentCancelReason? = TypeAdapter.toEnum(reason)

    @ToJson
    fun fromEnum(reason: AppointmentCancelReason?): String? = TypeAdapter.fromEnum(reason)
  }

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(reason: String?): AppointmentCancelReason? = TypeAdapter.toEnum(reason)

    @TypeConverter
    fun fromEnum(reason: AppointmentCancelReason?): String? = TypeAdapter.fromEnum(reason)
  }

  companion object {

    fun values(): Set<AppointmentCancelReason> {
      return TypeAdapter.KNOWN_MAPPINGS.keys
    }

    @VisibleForTesting
    fun random(apiV2Enabled: Boolean): AppointmentCancelReason {
      return if (apiV2Enabled) {
        TypeAdapter.KNOWN_MAPPINGS.keys.shuffled().first()
      } else {
        listOf(PatientNotResponding, Moved, Dead, Other).shuffled().first()
      }
    }
  }
}
