package org.simple.clinic.home

sealed class HomeScreenEffect

object OpenFacilitySelection : HomeScreenEffect()

object LoadCurrentFacility : HomeScreenEffect()
