package org.simple.clinic.facility.change

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.facility.change.FacilityListItem.Address
import org.simple.clinic.facility.change.FacilityListItem.Builder
import org.simple.clinic.facility.change.FacilityListItem.Name
import org.simple.clinic.patient.PatientMocker

class FacilityListItemBuilderTest {

  @Test
  fun `search query should be correctly highlighted`() {
    val facility = PatientMocker.facility(
        name = "Facility Gotham",
        streetAddress = null,
        district = "Gotham City",
        state = "Gotham City")

    val template = FacilityListItem(
        facility = facility,
        address = Address.WithoutStreet(district = "Gotham City", state = "Gotham City"),
        name = Name.Plain(facility.name))

    val searchQuery1 = ""
    val listItem1 = Builder.build(facility, searchQuery1)
    assertThat(listItem1).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery2 = "Death"
    val listItem2 = Builder.build(facility, searchQuery2)
    assertThat(listItem2).isEqualTo(template.copy(name = Name.Plain(facility.name)))

    val searchQuery3 = "Goth"
    val listItem3 = Builder.build(facility, searchQuery3)
    assertThat(listItem3).isEqualTo(template.copy(name = Name.Highlighted(facility.name, highlightStart = 9, highlightEnd = 13)))
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

    val listItem1 = Builder.build(facilityWithStreet, searchQuery)
    assertThat(listItem1).isEqualTo(FacilityListItem(
        facility = facilityWithStreet,
        name = nameUiModel,
        address = Address.WithStreet(
            street = facilityWithStreet.streetAddress!!,
            district = facilityWithStreet.district,
            state = facilityWithStreet.state)))

    val facilityWithBlankStreet = facilityWithStreet.copy(streetAddress = " ")
    val listItem2 = Builder.build(facilityWithBlankStreet, searchQuery)
    assertThat(listItem2).isEqualTo(FacilityListItem(
        facility = facilityWithBlankStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithBlankStreet.district,
            state = facilityWithBlankStreet.state)))

    val facilityWithNullStreet = facilityWithStreet.copy(streetAddress = null)
    val listItem3 = Builder.build(facilityWithNullStreet, searchQuery)
    assertThat(listItem3).isEqualTo(FacilityListItem(
        facility = facilityWithNullStreet,
        name = nameUiModel,
        address = Address.WithoutStreet(
            district = facilityWithNullStreet.district,
            state = facilityWithNullStreet.state)))
  }
}
