package org.simple.clinic.util

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.analytics.NetworkConnectivityStatus
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional
import javax.inject.Inject

interface RequiresNetwork {
  var networkStatus: Optional<NetworkConnectivityStatus>

  val hasNetworkConnection
    get() = networkStatus == Optional.of(ACTIVE)
}

class RuntimeNetworkStatus<T : Any> @Inject constructor(
    private val networkCapabilitiesProvider: NetworkCapabilitiesProvider,
    private val schedulersProvider: SchedulersProvider
) : ObservableTransformer<T, T> {

  override fun apply(upstream: Observable<T>): ObservableSource<T> {
    val sharedUpstream = upstream.share()

    val eventsRequiringNetwork = sharedUpstream.ofType<RequiresNetwork>()
    val eventsNotRequiringNetwork = sharedUpstream.filter { it !is RequiresNetwork }

    return Observable.merge(
        eventsNotRequiringNetwork,
        networkCheck(eventsRequiringNetwork)
    )
  }

  @Suppress("UNCHECKED_CAST")
  private fun networkCheck(events: Observable<RequiresNetwork>): ObservableSource<out T> {
    return events
        .cast<RequiresNetwork>()
        .observeOn(schedulersProvider.io())
        .map { event ->
          val networkConnectivityStatus = networkCapabilitiesProvider.networkConnectivityStatus()
          event.apply {
            networkStatus = Optional.of(networkConnectivityStatus)
          } as T
        }
  }
}
