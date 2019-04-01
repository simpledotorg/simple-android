package org.simple.clinic.home

import dagger.Module
import dagger.Provides
import io.reactivex.Observable

@Module
open class HomeModule {

  @Provides
  open fun provideHomeScreenConfig(): Observable<HomeScreenConfig> {
    return Observable.just(HomeScreenConfig(vs01Apr19HelpScreenEnabled = false))
  }
}
