package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Language : Parcelable

@Parcelize
data class ProvidedLanguage(val displayName: String, val languageCode: String) : Language()

@Parcelize
object SystemDefaultLanguage : Language()
