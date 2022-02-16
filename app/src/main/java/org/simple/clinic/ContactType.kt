package org.simple.clinic

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class ContactType {

  object WhatsApp : ContactType()

  object Telegram : ContactType()

  data class Unknown(val actualValue: String) : ContactType()

  object TypeAdapter : SafeEnumTypeAdapter<ContactType>(
      knownMappings = mapOf(
          WhatsApp to "whatsapp",
          Telegram to "telegram"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): ContactType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(contactType: ContactType): String? = TypeAdapter.fromEnum(contactType)
  }
}
