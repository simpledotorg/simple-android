package experiments

import dagger.Subcomponent

@Subcomponent(modules = [ExperimentsModule::class])
interface ExperimentsComponent {
  fun inject(target: experiments.instantsearch.PatientSearchScreen)

  @Subcomponent.Builder
  interface Builder {
    fun build(): ExperimentsComponent
  }
}
