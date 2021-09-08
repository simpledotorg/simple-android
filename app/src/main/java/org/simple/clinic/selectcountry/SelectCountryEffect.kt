package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment

sealed class SelectCountryEffect

object FetchManifest : SelectCountryEffect()

data class SaveCountryEffect(val country: Country) : SelectCountryEffect()

object GoToStateSelectionScreen : SelectCountryEffect()

data class SaveDeployment(val deployment: Deployment) : SelectCountryEffect()

object GoToRegistrationScreen : SelectCountryEffect()
