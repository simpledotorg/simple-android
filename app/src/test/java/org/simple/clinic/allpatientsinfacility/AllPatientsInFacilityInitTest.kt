package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityModel.Companion.FETCHING_PATIENTS
import org.simple.clinic.TestData

class AllPatientsInFacilityInitTest {
  private val initSpec = InitSpec<AllPatientsInFacilityModel, AllPatientsInFacilityEffect>(AllPatientsInFacilityInit())
  private val fetchingPatientsModel = FETCHING_PATIENTS

  @Test
  fun `it can fetch a patients in a facility during screen creation`() {
    initSpec
        .whenInit(fetchingPatientsModel)
        .then(
            assertThatFirst(
                hasModel(fetchingPatientsModel),
                hasEffects(FetchFacilityEffect as AllPatientsInFacilityEffect)
            )
        )
  }

  /**
   * We should be restoring the existing state. However, since we are running into `TransactionTooLargeException`,
   * we have decided not to save the list of patients inside the model. Which means, we should query this information
   * again from the database.
   */
  @Test
  fun `it can fetch the patients in a facility during state restoration`() {
    val noPatientsModel = fetchingPatientsModel
        .facilityFetched(TestData.facility())
        .noPatients()

    initSpec
        .whenInit(noPatientsModel)
        .then(
            assertThatFirst(
                hasModel(fetchingPatientsModel),
                hasEffects(FetchFacilityEffect as AllPatientsInFacilityEffect)
            )
        )
  }
}
