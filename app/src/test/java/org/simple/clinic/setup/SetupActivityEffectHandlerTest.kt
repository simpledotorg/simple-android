package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.DATABASE_NAME
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.setup.runcheck.AllowApplicationToRun
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed.Reason
import org.simple.clinic.storage.DatabaseEncryptor
import org.simple.clinic.user.User
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.Optional
import java.util.UUID

class SetupActivityEffectHandlerTest {

  private val onboardingCompletePreference = mock<Preference<Boolean>>()
  private val uiActions = mock<UiActions>()
  private val userDao = mock<User.RoomDao>()
  private val appConfigRepository = mock<AppConfigRepository>()
  private val appDatabase = mock<org.simple.clinic.AppDatabase>()
  private val databaseMaintenanceRunAtPreference = mock<Preference<Optional<Instant>>>()
  private val clock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val userClock = TestUserClock(Instant.parse("2021-07-11T00:00:00Z"))
  private val allowApplicationToRun = mock<AllowApplicationToRun>()
  private val loadV1Country = mock<LoadV1Country>()
  private val databaseEncryptor = mock<DatabaseEncryptor>()

  private val effectHandler = SetupActivityEffectHandler(
      uiActions = uiActions,
      userDao = userDao,
      appConfigRepository = appConfigRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      appDatabase = appDatabase,
      clock = clock,
      allowApplicationToRun = allowApplicationToRun,
      onboardingCompletePreference = onboardingCompletePreference,
      databaseMaintenanceRunAt = databaseMaintenanceRunAtPreference,
      userClock = userClock,
      loadV1Country = loadV1Country,
      databaseEncryptor = databaseEncryptor
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the user details must be fetched when the fetch user details effect is received`() {
    // given
    whenever(onboardingCompletePreference.get()) doReturn true

    val user = TestData.loggedInUser(uuid = UUID.fromString("426d2eb9-ebf7-4a62-b157-1de221c7c3d0"))
    whenever(userDao.userImmediate()).doReturn(user)

    val country = TestData.country()
    whenever(appConfigRepository.currentCountry()) doReturn country

    val v1Country = mapOf(
        "country_code" to "IN",
        "endpoint" to "https://api.simple.org/api/v1",
        "display_name" to "India",
        "isd_code" to "91"
    )
    whenever(loadV1Country.load()).thenReturn(Optional.of(v1Country))

    val currentDeployment = TestData.deployment()
    whenever(appConfigRepository.currentDeployment()).thenReturn(currentDeployment)

    // when
    testCase.dispatch(FetchUserDetails)

    // then
    testCase.assertOutgoingEvents(UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.of(country),
        userSelectedCountryV1 = Optional.of(v1Country),
        currentDeployment = Optional.of(currentDeployment)
    ))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the go to main activity effect is received, the main activity must be opened`() {
    // when
    testCase.dispatch(GoToMainActivity)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToMainActivity()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show onboarding screen effect is received, the splash screen must be shown`() {
    // when
    testCase.dispatch(ShowOnboardingScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showSplashScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the initialize database screen effect is received, the database must be initialized`() {
    // given
    whenever(userDao.userCount()) doReturn Single.just(0)

    // when
    testCase.dispatch(InitializeDatabase)

    // then
    testCase.assertOutgoingEvents(DatabaseInitialized)
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `the country selection screen must be opened when the show country selection effect is received`() {
    // when
    testCase.dispatch(ShowCountrySelectionScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showCountrySelectionScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the run database maintenance effect is received, the database must be pruned`() {
    // when
    testCase.dispatch(RunDatabaseMaintenance)

    // then
    verify(appDatabase).prune(Instant.now(userClock))
    verify(databaseMaintenanceRunAtPreference).set(Optional.of(Instant.now(clock)))
    testCase.assertOutgoingEvents(DatabaseMaintenanceCompleted)
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the load database maintenance last run at time effect is received, the last run timestamp must be loaded`() {
    // given
    val databaseMaintenanceLastRunAt = Optional.of(Instant.parse("2018-01-01T00:00:00Z"))
    whenever(databaseMaintenanceRunAtPreference.get()).thenReturn(databaseMaintenanceLastRunAt)

    // when
    testCase.dispatch(FetchDatabaseMaintenanceLastRunAtTime)

    // then
    testCase.assertOutgoingEvents(DatabaseMaintenanceLastRunAtTimeLoaded(databaseMaintenanceLastRunAt))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the show not allowed to run message effect is received, show the not allowed to run message`() {
    // given
    val reason = Reason.Rooted

    // when
    testCase.dispatch(ShowNotAllowedToRunMessage(reason))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showDisallowedToRunError(reason)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the check application allowed to run effect is received, the application allowed to run check must be performed`() {
    // given
    val allowedToRun = Allowed
    whenever(allowApplicationToRun.check()).thenReturn(allowedToRun)

    // when
    testCase.dispatch(CheckIfAppCanRun)

    // then
    testCase.assertOutgoingEvents(AppAllowedToRunCheckCompleted(allowedToRun))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the save country and deployment effect is received, the country and deployment must be saved`() {
    // given
    val deploymentToSave = TestData.deployment()
    val countryToSave = TestData.country(deployments = listOf(deploymentToSave))

    // when
    testCase.dispatch(SaveCountryAndDeployment(countryToSave, deploymentToSave))

    // then
    verify(appConfigRepository).saveCurrentCountry(countryToSave)
    verify(appConfigRepository).saveDeployment(deploymentToSave)
    verifyNoMoreInteractions(appConfigRepository)

    testCase.assertOutgoingEvents(CountryAndDeploymentSaved)

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when delete stored country v1 effect is received, then delete stored country v1 preference`() {
    // when
    testCase.dispatch(DeleteStoredCountryV1)

    // then
    verify(appConfigRepository).deleteStoredCountryV1()
    verifyNoMoreInteractions(appConfigRepository)

    testCase.assertOutgoingEvents(StoredCountryV1Deleted)

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when execute database encryption effect is received, then execute database encryption`() {
    // when
    testCase.dispatch(ExecuteDatabaseEncryption)

    // then
    verify(databaseEncryptor).execute(DATABASE_NAME)
    verifyNoMoreInteractions(databaseEncryptor)

    testCase.assertOutgoingEvents(DatabaseEncryptionFinished)

    verifyNoInteractions(uiActions)
  }
}
