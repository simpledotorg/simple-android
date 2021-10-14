package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

sealed class SelectCountryEffect

object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: Country) : SelectCountryEffect()

sealed class SelectCountryViewEffect : SelectCountryEffect()

object GoToStateSelectionScreen : SelectCountryViewEffect()
