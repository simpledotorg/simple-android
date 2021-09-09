# ADR 006: Forward compatibility for persistable enums from Server responses

## Status

Accepted

## Context

RESTful APIs tend to return enumerated values to represent properties with a fixed number of well-defined values. These are usually represented
as `enum` classes on the client. When new business use cases are introduced, there could be additions to these values. If these values are consumed on
the fly and are not persisted on the client, they could simply be treated as a special case (unknown) or ignored all together.

Simple is an "offline-first" application and some of these values have to be persisted to the database. Hence, these enumerated values cannot be
represented using a Kotlin `enum` class. Offline-first means, there are some unique constraints that we have to deal with.

Apps on an older app version should be able to handle new enum values added to a type even though they don't know how to interpret the new value. This
could be solved by adding an UNKNOWN fallback value which the server adds as well. Except,

* Records are shared between different users and thus, between different app versions. A newer app version that understands the new enum value could
  create a record with the new value and upload it, but an older app version that does not know the new value will overwrite it with UNKNOWN and this
  value will overwrite the actual value on the server as well when it syncs.

* An app on an older version could have a degraded, fallback experience for enum values it does not understand, but as soon as it updates, the app
  should be able to interpret the new values as if it was synced afresh.

## Decision

To maintain forward-compatibility with enum values, we have to roll out our own infrastructure. To accommodate, we use sealed classes to represent
enumerated values so that,

- We could save these values to the database.
- Use these values in newer versions of the app.
- Send the same enum values back to the server during sync even though the current version of the app cannot interpret these values during runtime.

In order or achieve this, we require 4 classes.

1. A sealed class hierarchy that is used to represent the enumerated values. Which also includes an `Unknown(val actualValue: String)` data class
   which is used to capture and handle newly introduced values.
2. An implementation of the `SafeEnumTypeAdapter` class.
3. A `RoomTypeConverter` to safely store and retrieve newly added enum values.
4. A `MoshiTypeAdapter` to serialize and deserialize newly added enum values.

The interesting part is the `SafeEnumTypAdapter` class definition,

```kotlin
open class SafeEnumTypeAdapter<T>(
    val knownMappings: Map<T, String>,
    private val unknownEnumToStringConverter: (T) -> String,
    private val unknownStringToEnumConverter: (String) -> T
) {
  // …
}
```

The `knownMappings` field takes in a `Map` that can convert to and from enumerations that the current version of the application understands.

The `unknownEnumToStringConverter` converts unknown enum values to their actual string representation (usually used when pushing data to the server).

The `unknownStringToEnumConverter` can wrap the newly introduced value into the `Unknown` sealed data class defined earlier.

## Consequences

Not all enumerated values have to conform to this forward compatibility spec and be represented in this fashion. Only enums that are served through
APIs **and** are saved to the database should use this convention.

The only way for developers to gain awareness about this, is to read this ADR or through tribal knowledge. Ideally, we should have a lint check that
throws an error if an `enum class` is involved both in a Retrofit and a Room interface.

## Reference

```kotlin
sealed class AppointmentType {

  object Manual : AppointmentType() // 1. Each subclass represents an enumerated value

  object Automatic : AppointmentType()

  data class Unknown(val actual: String) : AppointmentType() // 2. Defined to deal with forward-compatibility

  object TypeAdapter : SafeEnumTypeAdapter<AppointmentType>(
      knownMappings = mapOf( // 3. Know mappings, notice snakecase (lowercase + underscores)
          Manual to "manual",
          Automatic to "automatic"
      ),
      unknownStringToEnumConverter = { Unknown(it) }, // 4. Converts unknown enum values for forward-compatibility
      unknownEnumToStringConverter = { (it as Unknown).actual } // 5. Used during sync to send actual values back to the server.
  )

  class RoomTypeConverter { // 6. Room Type Converter

    @TypeConverter
    fun toEnum(value: String?) = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(enum: AppointmentType?) = TypeAdapter.fromEnum(enum)
  }

  class MoshiTypeAdapter { // 7. Moshi Type Adapter

    @FromJson
    fun toEnum(value: String?) = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(enum: AppointmentType) = TypeAdapter.fromEnum(enum)
  }

  companion object {
    @VisibleForTesting
    fun random() = AppointmentType.TypeAdapter.knownMappings.keys.shuffled().first()
  }
}
```

The `RoomTypeConverter` should be registered in the `AppDatabase` class and the `MoshiTypeAdapter` should be registered in the `NetworkModule`.
