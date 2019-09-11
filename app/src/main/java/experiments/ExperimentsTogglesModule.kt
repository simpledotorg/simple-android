package experiments

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ExperimentsTogglesModule {

  @Provides
  @Named("experiment_instantsearch_v1_toggle")
  fun instantSearchV1ExperimentToggle(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
    return rxSharedPreferences.getBoolean("experiment_instantsearch_v1_toggle", false)
  }
}
