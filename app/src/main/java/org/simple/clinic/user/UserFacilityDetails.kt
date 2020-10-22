package org.simple.clinic.user

import java.util.UUID

data class UserFacilityDetails(
    val userId: UUID,
    val currentFacilityId: UUID,
    val currentSyncGroupId: String
)
