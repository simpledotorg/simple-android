package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.sync.DataPullResponse
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.threeten.bp.Duration
import org.threeten.bp.Period

@Suppress("UNCHECKED_CAST")
@RunWith(JUnitParamsRunner::class)
class AppointmentSyncTest {

  private val syncCoordinator = mock<SyncCoordinator>()
  private val repository = mock<AppointmentRepository>()
  private val apiV1 = mock<AppointmentSyncApiV1>()
  private val apiV2 = mock<AppointmentSyncApiV2>()
  private val lastPullToken = mock<Preference<Optional<String>>>()

  private lateinit var config: AppointmentConfig

  private lateinit var sync: AppointmentSync

  @Before
  fun setup() {
    sync = AppointmentSync(syncCoordinator, repository, apiV1, apiV2, Single.fromCallable { config }, lastPullToken)
  }

  @Test
  @Parameters("true", "false")
  fun `when pulling appointments, v2 API should only be called if its enabled`(v2ApiEnabled: Boolean) {
    whenever(syncCoordinator.pull(eq(repository), eq(lastPullToken), any())).thenAnswer { invocation ->
      val pullNetworkCall = invocation.arguments[2] as (Int, String?) -> Single<out DataPullResponse<Appointment>>
      pullNetworkCall.invoke(10, "lastPullToken")
      Completable.complete()
    }

    config = AppointmentConfig(
        v2ApiEnabled = v2ApiEnabled,
        minimumOverduePeriodForHighRisk = Period.ofDays(1),
        overduePeriodForLowestRiskLevel = Period.ofDays(1))

    sync.pull().blockingAwait()

    if (v2ApiEnabled) {
      verify(apiV2).pull(any(), any())
    } else {
      verify(apiV1).pull(any(), any())
    }
  }

  @Test
  @Parameters("true", "false")
  fun `when pushing appointments, v2 API should only be called if its enabled`(v2ApiEnabled: Boolean) {
    whenever(syncCoordinator.push(eq(repository), any())).thenAnswer { invocation ->
      val pushNetworkCall = invocation.arguments[1] as (List<Appointment>) -> Single<DataPushResponse>
      pushNetworkCall.invoke(emptyList())
      Completable.complete()
    }

    config = AppointmentConfig(
        v2ApiEnabled = v2ApiEnabled,
        minimumOverduePeriodForHighRisk = Period.ofDays(1),
        overduePeriodForLowestRiskLevel = Period.ofDays(1))

    sync.push().blockingAwait()

    if (v2ApiEnabled) {
      verify(apiV2).push(any())
    } else {
      verify(apiV1).push(any())
    }
  }
}
