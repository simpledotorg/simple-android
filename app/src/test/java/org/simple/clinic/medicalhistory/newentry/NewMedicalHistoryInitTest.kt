package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country

class NewMedicalHistoryInitTest {

  private val country = TestData.country(isoCountryCode = Country.INDIA)
  private val defaultModel = NewMedicalHistoryModel.default(country)

  private val initSpec = InitSpec(NewMedicalHistoryInit())

  @Test
  fun `when the screen is created, the ongoing patient entry and the current facility must be loaded`() {
    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(LoadOngoingPatientEntry, LoadCurrentFacility)
            )
        )
  }
}
