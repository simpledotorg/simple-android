package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

sealed class SelectCountryEffect

data object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: Country) : SelectCountryEffect()

sealed class SelectCountryViewEffect : SelectCountryEffect()

data object GoToStateSelectionScreen : SelectCountryViewEffect()

data object ReplaceCurrentScreenWithStateSelectionScreen : SelectCountryViewEffect()
