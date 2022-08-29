package org.simple.clinic

import android.os.Parcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class ContactType : Parcelable {

  @Parcelize
  object WhatsApp : ContactType()

  @Parcelize
  object Telegram : ContactType()

  @Parcelize
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
