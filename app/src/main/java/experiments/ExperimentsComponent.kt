package experiments

import dagger.Subcomponent

@Subcomponent(modules = [ExperimentsModule::class])
interface ExperimentsComponent {
  fun inject(target: experiments.instantsearch.PatientSearchScreen)
  fun inject(target: ExperimentsToggleActivity.ExperimentsToggleFragment)

  @Subcomponent.Builder
  interface Builder {
    fun build(): ExperimentsComponent
  }
}
