package org.simple.clinic.appupdate

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ContactType

@Parcelize
@JsonClass(generateAdapter = true)
data class AppUpdateHelpContact(
    @Json(name = "display_text")
    val displayText: String,
    val url: String,
    @Json(name = "contact_type")
    val contactType: ContactType
) : Parcelable
