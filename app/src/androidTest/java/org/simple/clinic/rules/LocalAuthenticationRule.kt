package org.simple.clinic.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import java.util.UUID
import javax.inject.Inject

/**
 * Runs every test with a local user.
 **/
class LocalAuthenticationRule : TestRule {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var appDatabase: AppDatabase

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@LocalAuthenticationRule)

        try {
          createLocalUser()
          base.evaluate()

        } finally {
          appDatabase.clearAllTables()
        }
      }
    }
  }

  private fun createLocalUser() {
    val registrationFacilityUuid = UUID.fromString("2c9846a0-3482-413c-be4f-f529ace55557")

    val facilities = listOf(
        testData.facility(registrationFacilityUuid),
        testData.facility(UUID.fromString("cb143bdc-1872-459e-88fd-30ea0d6e1659")),
        testData.facility(UUID.fromString("db332209-d4cd-4c52-8082-37dab6518941"))
    )
    appDatabase.facilityDao().save(facilities)

    val user = testData.loggedInUser(
        uuid = UUID.fromString("bc60515e-4bbe-4cf0-a2e2-a326dbbb84ee"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        registrationFacilityUuid = registrationFacilityUuid,
        currentFacilityUuid = registrationFacilityUuid
    )
    userSession.storeUser(user).blockingAwait()
  }
}
