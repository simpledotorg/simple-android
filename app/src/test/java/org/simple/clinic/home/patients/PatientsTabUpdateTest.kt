package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Optional
import java.util.UUID

class PatientsTabUpdateTest {

  private val spec = UpdateSpec(PatientsTabUpdate())

  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("89865fa0-f6e1-48e6-b4d3-206584bb708c"),
      status = UserStatus.ApprovedForSyncing,
      loggedInStatus = User.LoggedInStatus.LOGGED_IN
  )

  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `when the patient short code is entered, the short code search screen must be opened`() {
    val model = defaultModel
        .userLoaded(user)
        .numberOfPatientsRegisteredUpdated(0)

    val shortCode = "1234567"

    spec
        .given(model)
        .whenEvent(BusinessIdScanned.ByShortCode(shortCode))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenShortCodeSearchScreen(shortCode))
        ))
  }

  @Test
  fun `when the patient identifier is scanned, a patient with the given identifier must be searched for`() {
    val model = defaultModel
        .userLoaded(user)
        .numberOfPatientsRegisteredUpdated(0)

    val identifier = TestData.identifier("88d12415-b10d-4ebb-bf48-482ece022139", BpPassport)

    spec
        .given(model)
        .whenEvent(BusinessIdScanned.ByIdentifier(identifier))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SearchPatientByIdentifier(identifier))
        ))
  }

  @Test
  fun `when the scanned identifier has a corresponding patient, the patient summary screen must be opened`() {
    val model = defaultModel
        .userLoaded(user)
        .numberOfPatientsRegisteredUpdated(0)

    val identifier = TestData.identifier("88d12415-b10d-4ebb-bf48-482ece022139", BpPassport)
    val patient = TestData.patient(uuid = UUID.fromString("614a3a62-92be-4551-92d0-beca649cfd7c"))

    val event = PatientSearchByIdentifierCompleted(
        foundPatient = Optional.of(patient),
        searchedIdentifier = identifier
    )

    spec
        .given(model)
        .whenEvent(event)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patient.uuid))
        ))
  }
}
