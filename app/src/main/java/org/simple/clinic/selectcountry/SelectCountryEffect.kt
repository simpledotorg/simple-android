package org.simple.clinic.selectcountry

sealed class SelectCountryEffect

object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: Country) : SelectCountryEffect()
