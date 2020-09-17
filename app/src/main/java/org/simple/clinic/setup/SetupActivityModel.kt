package org.simple.clinic.setup

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.appconfig.Country
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.isNotEmpty

@Parcelize
data class SetupActivityModel(
    // I don't like using nullable booleans here, but if I were to add the User model as well, it
    // would require me to make that Parcelable as well, which would then cascade down to many
    // classes. Since for the purposes of this screen, all we need is whether a user is logged in
    // and whether they have selected a country, I figured this is good enough for now until we
    // find a better way.
    // TODO(vs): 2019-11-08 Figure out a better way, maybe a separate UI parcelable model for the user?
    val isUserLoggedIn: Boolean?,
    val hasUserSelectedACountry: Boolean?
) : Parcelable {

  companion object {
    fun create(): SetupActivityModel {
      return SetupActivityModel(isUserLoggedIn = null, hasUserSelectedACountry = null)
    }
  }

  fun withLoggedInUser(user: Optional<User>): SetupActivityModel {
    return copy(isUserLoggedIn = user.isNotEmpty())
  }

  fun withSelectedCountry(country: Optional<Country>): SetupActivityModel {
    return copy(hasUserSelectedACountry = country.isNotEmpty())
  }
}
