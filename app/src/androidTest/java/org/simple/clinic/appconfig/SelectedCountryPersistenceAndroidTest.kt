package org.simple.clinic.appconfig

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.storage.SharedPreferencesMode
import org.simple.clinic.storage.SharedPreferencesMode.Mode.Default
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import java.util.Optional
import javax.inject.Inject


class SelectedCountryPersistenceAndroidTest {

  @get:Rule
  val rules = Rules.global()

  @Inject
  @SharedPreferencesMode(Default)
  lateinit var preferences: SharedPreferences

  @Inject
  lateinit var selectedCountryPreference: Preference<Optional<Country>>

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun persisted_selected_country_should_be_read() {
    // If this test fails, it means that you have changed the structure of the Country class
    // without migrating the shared preference and will break the app when an update happens.
    val savedJson = """
      {
        "country_code": "IN",
        "display_name": "India",
        "isd_code": "91",
        "deployments": [
          {
            "display_name": "IHCI",
            "endpoint": "https://in.simple.org/"
          }
        ]
      }
    """

    preferences
        .edit()
        .putString("preference_selected_country_v2", savedJson)
        .commit()

    val expectedSavedCountry = TestData.country(
        isoCountryCode = "IN",
        deploymentEndPoint = "https://in.simple.org/",
        deploymentName = "IHCI",
        displayName = "India",
        isdCode = "91"
    )
    assertThat(selectedCountryPreference.get())
        .isEqualTo(Optional.of(expectedSavedCountry))
  }
}
