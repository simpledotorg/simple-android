package org.simple.clinic.main

import javax.inject.Qualifier

@Qualifier
annotation class TypedPreference(val value: Type) {

  enum class Type {
    OnboardingComplete,
    DatabaseMaintenanceRunAt,
    MedicalRegistrationId,
    FacilitySyncGroupSwitchedAt,
    LastDrugPullToken,
    SelectedState,
    CountryV1,
    IsLightAppUpdateNotificationShown,
    IsMediumAppUpdateNotificationShown
  }
}
