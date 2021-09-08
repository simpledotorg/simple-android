package org.simple.clinic.selectcountry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class SelectCountryUpdate : Update<SelectCountryModel, SelectCountryEvent, SelectCountryEffect> {

  override fun update(
      model: SelectCountryModel,
      event: SelectCountryEvent
  ): Next<SelectCountryModel, SelectCountryEffect> {
    return when (event) {
      is ManifestFetched -> next(model.manifestFetched(event.countries))
      is ManifestFetchFailed -> next(model.manifestFetchError(event.error))
      is CountryChosen -> next(model.countryChosen(event.country))
      NextClicked -> dispatch(SaveCountryEffect(model.selectedCountry!!))
      CountrySaved -> countrySaved(model)
      RetryClicked -> next(model.fetching(), setOf(FetchManifest))
      DeploymentSaved -> dispatch(GoToRegistrationScreen)
    }
  }

  private fun countrySaved(model: SelectCountryModel): Next<SelectCountryModel, SelectCountryEffect> {
    val effect = if (!model.hasMoreThanOneDeployment) {
      val selectedCountryDeployment = model.selectedCountry!!.deployments.first()
      SaveDeployment(selectedCountryDeployment)
    } else {
      GoToStateSelectionScreen
    }

    return dispatch(effect = effect)
  }
}
