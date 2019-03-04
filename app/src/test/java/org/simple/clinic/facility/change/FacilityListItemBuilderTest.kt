package org.simple.clinic.facility.change

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Address
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.DistanceCalculator
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RxErrorsRule

class FacilityListItemBuilderTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val distanceCalculator = mock<DistanceCalculator>()
  private val listItemBuilder = FacilityListItemBuilder(distanceCalculator)

  @Test
  fun `search query should be correctly highlighted`() {
    val facility = PatientMocker.facility(
        name = "Facility Gotham",
        streetAddress = null,
        district = "Gotham City",
        state = "Gotham City")

    val template = FacilityOption(
        facility = facility,
        address = Address.WithoutStreet(district = "Gotham City", state = "Gotham City"),
        name = Name.Plain(facility.name))

    val searchQuery1 = ""
    val listItems1 = listItemBuilder.build(listOf(facility), searchQuery1).first()
    assertThat(listItems1).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery2 = "Death"
    val listItems2 = listItemBuilder.build(listOf(facility), searchQuery2).first()
    assertThat(listItems2).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery3 = "Goth"
    val listItems3 = listItemBuilder.build(listOf(facility), searchQuery3).first()
    assertThat(listItems3).isEqualTo(template.copy(name = Name.Highlighted(facility.name, highlightStart = 9, highlightEnd = 13)))
  }

  @Test
  fun `address should be correctly created`() {
    val facilityWithStreet = PatientMocker.facility(
        name = "Facility Gotham",
        streetAddress = "Gotham",
        district = "Gotham City",
        state = "Gotham City")

    val nameUiModel = Name.Plain(facilityWithStreet.name)
    val searchQuery = ""

    val listItem1 = listItemBuilder.build(listOf(facilityWithStreet), searchQuery).first()
    assertThat(listItem1).isEqualTo(FacilityOption(
        facility = facilityWithStreet,
        name = nameUiModel,
        address = Address.WithStreet(
            street = facilityWithStreet.streetAddress!!,
            district = facilityWithStreet.district,
            state = facilityWithStreet.state)))

    val facilityWithBlankStreet = facilityWithStreet.copy(streetAddress = " ")
    val listItem2 = listItemBuilder.build(listOf(facilityWithBlankStreet), searchQuery).first()
    assertThat(listItem2).isEqualTo(FacilityOption(
        facility = facilityWithBlankStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithBlankStreet.district,
            state = facilityWithBlankStreet.state)))

    val facilityWithNullStreet = facilityWithStreet.copy(streetAddress = null)
    val listItem3 = listItemBuilder.build(listOf(facilityWithNullStreet), searchQuery).first()
    assertThat(listItem3).isEqualTo(FacilityOption(
        facility = facilityWithNullStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithNullStreet.district,
            state = facilityWithNullStreet.state)))
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  fun `when user location is present and search query is empty then facilities nearby user should be correctly identified`() {
    val userLocation = Coordinates(latitude = 1.0, longitude = 1.0)

    val facility1 = PatientMocker.facility(
        name = "Exactly at proximity threshold",
        location = Coordinates(0.0, 0.0))

    val facility2 = PatientMocker.facility(
        name = "Within proximity threshold",
        location = Coordinates(2.0, 2.0))

    val facility3 = PatientMocker.facility(
        name = "Without location",
        location = null)

    val facility4 = PatientMocker.facility(
        name = "Outside of proximity threshold",
        location = Coordinates(100.0, 100.0))

    val proximityThreshold = Distance.ofKilometers(2.0)
    whenever(distanceCalculator.between(userLocation, facility1.location!!)).thenReturn(Distance.ofKilometers(2.0))
    whenever(distanceCalculator.between(userLocation, facility2.location!!)).thenReturn(Distance.ofKilometers(1.0))
    whenever(distanceCalculator.between(userLocation, facility4.location!!)).thenReturn(Distance.ofKilometers(100.0))

    val searchQuery = ""

    val facilities = listOf(facility1, facility2, facility3, facility4)
    val facilityListItems = listItemBuilder.build(facilities, searchQuery, userLocation, proximityThreshold)
    val facilityNameOrHeaders = facilityListItems.map {
      when (it) {
        is FacilityListItem.Header -> it
        is FacilityOption -> it.name.text
      }
    }

    assertThat(facilityNameOrHeaders).isEqualTo(listOf(
        FacilityListItem.Header.SuggestedFacilities(hasSpacingWithPreviousSection = false),
        "Within proximity threshold",
        "Exactly at proximity threshold",
        FacilityListItem.Header.AllFacilities(hasSpacingWithPreviousSection = true),
        "Exactly at proximity threshold",
        "Within proximity threshold",
        "Without location",
        "Outside of proximity threshold"
    ))
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  fun `when both user location and search query are present then nearby facilities should not be present`() {
    val userLocation = Coordinates(latitude = 1.0, longitude = 1.0)

    val facility1 = PatientMocker.facility(
        name = "Within proximity threshold",
        location = Coordinates(0.0, 0.0))

    val facility2 = PatientMocker.facility(
        name = "Outside of proximity threshold",
        location = Coordinates(2.0, 2.0))

    val proximityThreshold = Distance.ofKilometers(2.0)
    whenever(distanceCalculator.between(userLocation, facility1.location!!)).thenReturn(Distance.ofKilometers(1.0))
    whenever(distanceCalculator.between(userLocation, facility2.location!!)).thenReturn(Distance.ofKilometers(3.0))

    val searchQuery = "Gotham must burn"

    val facilities = listOf(facility1, facility2)
    val facilityListItems = listItemBuilder.build(facilities, searchQuery, userLocation, proximityThreshold)

    val facilityNameOrHeaders = facilityListItems.map {
      when (it) {
        is FacilityListItem.Header -> it
        is FacilityOption -> it.name.text
      }
    }

    assertThat(facilityNameOrHeaders).isEqualTo(listOf(
        "Within proximity threshold",
        "Outside of proximity threshold"
    ))
  }

  @Test
  fun `when user location is absent then facilities nearby user should be empty and headers should not be present`() {
    val userLocation = null

    val facilities = listOf(
        PatientMocker.facility(
            name = "With location",
            location = Coordinates(latitude = 51.864038, longitude = 17.608030)
        ),
        PatientMocker.facility(
            name = "Without location",
            location = null
        ))

    val searchQuery = "with"
    val proximityThreshold = Distance.ofKilometers(2.0)

    val facilityListItems = listItemBuilder.build(facilities, searchQuery, userLocation, proximityThreshold)
    val facilityNames = facilityListItems.map { (it as FacilityOption).name.text }

    assertThat(facilityNames).isEqualTo(listOf(
        "With location",
        "Without location"
    ))
  }
}
