package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.util.scheduler.SchedulersProvider

class CustomDrugEntryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: CustomDrugEntrySheetUiActions
) {
  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: CustomDrugEntrySheetUiActions
    ): CustomDrugEntryEffectHandler
  }

  fun build(): ObservableTransformer<CustomDrugEntryEffect, CustomDrugEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomDrugEntryEffect, CustomDrugEntryEvent>()
        .addConsumer(ShowEditFrequencyDialog::class.java, { uiActions.showEditFrequencyDialog(it.frequency) }, schedulersProvider.ui())
        .addConsumer(SetDrugFrequency::class.java, { uiActions.setDrugFrequency(it.frequency) }, schedulersProvider.ui())
        .addConsumer(SetSheetTitle::class.java, ::setSheetTitle, schedulersProvider.ui())
        .build()
  }

  private fun setSheetTitle(setSheetTitle: SetSheetTitle) {
    val sheetTitle = constructSheetTitle(setSheetTitle.name, setSheetTitle.dosage, setSheetTitle.frequency)

    uiActions.setSheetTitle(sheetTitle)
  }

  private fun constructSheetTitle(
      name: String?,
      dosage: String?,
      frequency: DrugFrequency?
  ): String {
    return listOfNotNull(name, dosage, frequency?.toString()).joinToString()
  }
}
