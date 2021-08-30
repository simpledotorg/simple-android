package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.CountryV2

sealed class SelectCountryEffect

object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: CountryV2) : SelectCountryEffect()

object GoToNextScreen : SelectCountryEffect()
