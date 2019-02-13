package org.simple.clinic.facility.change

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Address
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name
import org.simple.clinic.location.Coordinates
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RxErrorsRule

class FacilityListItemBuilderTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

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
    val listItems1 = FacilityListItemBuilder.build(listOf(facility), searchQuery1).first()
    assertThat(listItems1).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery2 = "Death"
    val listItems2 = FacilityListItemBuilder.build(listOf(facility), searchQuery2).first()
    assertThat(listItems2).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery3 = "Goth"
    val listItems3 = FacilityListItemBuilder.build(listOf(facility), searchQuery3).first()
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

    val listItem1 = FacilityListItemBuilder.build(listOf(facilityWithStreet), searchQuery).first()
    assertThat(listItem1).isEqualTo(FacilityOption(
        facility = facilityWithStreet,
        name = nameUiModel,
        address = Address.WithStreet(
            street = facilityWithStreet.streetAddress!!,
            district = facilityWithStreet.district,
            state = facilityWithStreet.state)))

    val facilityWithBlankStreet = facilityWithStreet.copy(streetAddress = " ")
    val listItem2 = FacilityListItemBuilder.build(listOf(facilityWithBlankStreet), searchQuery).first()
    assertThat(listItem2).isEqualTo(FacilityOption(
        facility = facilityWithBlankStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithBlankStreet.district,
            state = facilityWithBlankStreet.state)))

    val facilityWithNullStreet = facilityWithStreet.copy(streetAddress = null)
    val listItem3 = FacilityListItemBuilder.build(listOf(facilityWithNullStreet), searchQuery).first()
    assertThat(listItem3).isEqualTo(FacilityOption(
        facility = facilityWithNullStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithNullStreet.district,
            state = facilityWithNullStreet.state)))
  }

  @Test
  fun `when user location is present then facilities nearby user should be correctly identified`() {
    val userLocation = Coordinates(latitude = 51.919068, longitude = 17.647919)

    val facilities = listOf(
        PatientMocker.facility(
            name = "Exactly at proximity threshold",
            location = Coordinates(latitude = 51.864038, longitude = 17.608030)
        ),
        PatientMocker.facility(
            name = "Within proximity threshold",
            location = Coordinates(latitude = 51.914804, longitude = 17.649183)
        ),
        PatientMocker.facility(
            name = "Without location",
            location = null
        ),
        PatientMocker.facility(
            name = "Outside of proximity threshold",
            location = Coordinates(latitude = userLocation.latitude + 1, longitude = userLocation.longitude + 1)
        ))

    val searchQuery = "proximity"
    val proximityThreshold = Distance.ofKilometers(6.703436187871307)

    val facilityListItems = FacilityListItemBuilder.build(facilities, searchQuery, userLocation, proximityThreshold)

    assertThat(facilityListItems.map { it.name.text }).isEqualTo(listOf(
        "Within proximity threshold",
        "Exactly at proximity threshold",
        "Exactly at proximity threshold",
        "Within proximity threshold",
        "Without location",
        "Outside of proximity threshold"
    ))
  }

  @Test
  fun `when user location is absent then facilities nearby user should be empty`() {
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

    val facilityListItems = FacilityListItemBuilder.build(facilities, searchQuery, userLocation, proximityThreshold)

    assertThat(facilityListItems.map { it.name.text }).isEqualTo(listOf(
        "With location",
        "Without location"
    ))
  }
}
