package org.simple.clinic.instantsearch

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.util.matchers.IterableNotContaining.Companion.doesNotContain
import org.simple.sharedTestCode.TestData

class InstantSearchInitTest {

  private val initSpec = InitSpec(InstantSearchInit())
  private val identifier = TestData.identifier(
      value = "f16ebf24-14fb-46c7-9b34-b49cdc1c9453",
      type = BpPassport
  )
  private val defaultModel = InstantSearchModel.create(identifier, null, null)

  @Test
  fun `when screen is created, then load current facility`() {
    val model = InstantSearchModel.create(additionalIdentifier = null, patientPrefillInfo = null, searchQuery = null)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadCurrentFacility)
        ))
  }

  @Test
  fun `when screen is opened from blank qr code scan, then load current facility and open scanned qr code sheet`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel.scannedQrCodeSheetOpened()),
            hasEffects(LoadCurrentFacility, OpenScannedQrCodeSheet(identifier))
        ))
  }

  @Test
  fun `when screen is restored after receiving the scanned qr code sheet result, then do not open scanned qr code sheet`() {
    val model = defaultModel.scannedQrCodeSheetOpened()

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(doesNotContain(OpenScannedQrCodeSheet(identifier)))
        ))
  }
  
  @Test
  fun `when screen is created with a prefilled search query, then search with with search query`() {
    val searchQuery = "Ramesh Prasad"
    val model = InstantSearchModel.create(
        additionalIdentifier = null,
        patientPrefillInfo = null,
        searchQuery = searchQuery)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(ValidateSearchQuery(searchQuery = searchQuery))
        ))
  }
}
