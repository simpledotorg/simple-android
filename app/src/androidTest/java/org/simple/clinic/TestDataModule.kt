package org.simple.clinic

import dagger.Module
import dagger.Provides
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import javax.inject.Named

@Module
class TestDataModule {

  @Provides
  fun provideTestUser(userSession: UserSession): User {
    return userSession.loggedInUserImmediate()!!
  }

  @Provides
  @Named("user_pin")
  fun provideTestUserPin(): String = "1712"

  @Provides
  @Named("user_otp")
  fun provideTestUserOtp(): String = "000000"

  @Provides
  fun provideTestFacility(
      facilityRepository: FacilityRepository
  ): Facility {
    return facilityRepository.currentFacilityImmediate()!!
  }
}
