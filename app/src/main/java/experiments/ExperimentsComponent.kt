package experiments

import dagger.Subcomponent

@Subcomponent
interface ExperimentsComponent {
  fun inject(target: experiments.instantsearch.PatientSearchScreen)

  @Subcomponent.Builder
  interface Builder {
    fun build(): ExperimentsComponent
  }
}
