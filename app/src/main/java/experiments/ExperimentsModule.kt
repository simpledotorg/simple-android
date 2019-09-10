package experiments

import dagger.Module
import dagger.Provides
import experiments.instantsearch.InstantPatientSearchExperimentsDao
import org.simple.clinic.AppDatabase

@Module
class ExperimentsModule {

  @Provides
  fun providePatientSearchResultsDao(appDatabase: AppDatabase): InstantPatientSearchExperimentsDao {
    return InstantPatientSearchExperimentsDao(appDatabase)
  }
}
