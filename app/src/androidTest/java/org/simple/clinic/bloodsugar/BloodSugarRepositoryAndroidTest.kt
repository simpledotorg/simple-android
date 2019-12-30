package org.simple.clinic.bloodsugar

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.util.UUID
import javax.inject.Inject

class BloodSugarRepositoryAndroidTest {

  @Inject
  lateinit var repository: BloodSugarRepository

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var appDatabase: org.simple.clinic.AppDatabase

  @Inject
  lateinit var testData: TestData

  private val authenticationRule = LocalAuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.of(2000, Month.JANUARY, 1))
  }

  @Test
  fun saving_a_blood_sugar_reading_should_work_correctly() {
    //given
    val bloodSugarReading = BloodSugarReading(value = 10, type = Random)
    val patientUuid = UUID.fromString("a5921ec9-5c70-421a-bb0b-1291364683f6")
    val user = testData.qaUser()

    //when
    val testObserver = repository.saveMeasurement(
        reading = bloodSugarReading,
        patientUuid = patientUuid,
        loggedInUser = user,
        facility = testData.qaFacility(),
        recordedAt = Instant.now(clock)
    ).test()

    //then
    testObserver.assertComplete().assertNoErrors()
  }
}
