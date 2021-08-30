package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.CountryV2
import org.simple.clinic.appconfig.Deployment

sealed class SelectCountryEffect

object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: CountryV2) : SelectCountryEffect()

object GoToNextScreen : SelectCountryEffect()

data class SaveDeployment(val deployment: Deployment) : SelectCountryEffect()

object GoToRegistrationScreen : SelectCountryEffect()
