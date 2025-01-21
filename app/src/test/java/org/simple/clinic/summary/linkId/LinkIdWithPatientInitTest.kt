package org.simple.clinic.summary.linkId

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class LinkIdWithPatientInitTest {
  private val patientUuid = UUID.fromString("6828cf42-4cc5-44c7-a67c-ca9bb64890e8")
  private val identifier = TestData.identifier(
      type = Identifier.IdentifierType.BpPassport,
      value = "38491ca7-9d9a-44ef-a903-ab9d8891c54e"
  )

  private val defaultModel = LinkIdWithPatientModel.create(
      patientUuid = patientUuid,
      identifier = identifier
  )

  private val initSpec = InitSpec(LinkIdWithPatientInit())

  @Test
  fun `when screen is created, then get patient name`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(GetPatientNameFromId(patientUuid))
        ))
  }

  @Test
  fun `when screen is restored, then do nothing`() {
    val patientNameLoadedModel = defaultModel
        .patientNameFetched("Patient")

    initSpec
        .whenInit(patientNameLoadedModel)
        .then(assertThatFirst(
            hasModel(patientNameLoadedModel),
            hasNoEffects()
        ))
  }
}
