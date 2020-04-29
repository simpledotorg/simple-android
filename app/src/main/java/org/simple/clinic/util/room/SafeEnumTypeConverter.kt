package org.simple.clinic.util.room

/**
 * This class is meant to provide a safe way to (de)serialise enum values from network responses
 * without breaking older app versions that may not have support for a newly added enum value.
 **/
open class SafeEnumTypeAdapter<T>(
    val knownMappings: Map<T, String>,
    private val unknownEnumToStringConverter: (T) -> String,
    private val unknownStringToEnumConverter: (String) -> T
) {

  fun toEnum(value: String?): T? {
    return when {
      value.isNullOrBlank() -> null
      else -> knownMappings
          .entries
          .find { (_, stringValue) -> stringValue == value }
          ?.key ?: unknownStringToEnumConverter(value)
    }
  }

  fun fromEnum(enum: T?): String? {
    return when (enum) {
      null -> null
      !in knownMappings -> unknownEnumToStringConverter(enum)
      else -> knownMappings.getValue(enum)
    }
  }
}
