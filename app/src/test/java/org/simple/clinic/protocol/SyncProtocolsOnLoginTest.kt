package org.simple.clinic.protocol

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule

class SyncProtocolsOnLoginTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userSession = mock<UserSession>()
  private val protocolSync = mock<ProtocolSync>()
  private val protocolRepository = mock<ProtocolRepository>()

  private lateinit var syncProtocolOnLogin: SyncProtocolsOnLogin

  @Before
  fun setUp() {
    syncProtocolOnLogin = SyncProtocolsOnLogin(userSession, protocolSync, protocolRepository)
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
  }

  @Test
  fun `when user is not available then protocol drugs should not be synced`() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(None()))
    whenever(protocolRepository.recordCount()).thenReturn(Observable.just(1))

    syncProtocolOnLogin.listen()

    verify(protocolSync, never()).sync()
  }

  @Test
  fun `when user is available and existing drugs are empty then protocol drugs should be sync`() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(
        None(),
        Just(TestData.loggedInUser())))
    whenever(protocolRepository.recordCount()).thenReturn(Observable.just(0))

    syncProtocolOnLogin.listen()

    verify(protocolSync, times(1)).sync()
  }

  @Test
  fun `when user is available and existing drugs are not empty then protocol drugs should not be synced`() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(
        None(),
        Just(TestData.loggedInUser())))
    whenever(protocolRepository.recordCount()).thenReturn(Observable.just(1))

    syncProtocolOnLogin.listen()

    verify(protocolSync, never()).sync()
  }
}
