package org.simple.clinic.drugs

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.util.scheduler.SchedulersProvider

class EditMedicinesEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: EditMedicinesUiActions,
    private val schedulersProvider: SchedulersProvider
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: EditMedicinesUiActions): EditMedicinesEffectHandler
  }

  fun build(): ObservableTransformer<EditMedicinesEffect, EditMedicinesEvent> {
    return RxMobius
        .subtypeEffectHandler<EditMedicinesEffect, EditMedicinesEvent>()
        .addConsumer(ShowNewPrescriptionEntrySheet::class.java, { uiActions.showNewPrescriptionEntrySheet(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenDosagePickerSheet::class.java, { uiActions.showDosageSelectionSheet(it.drugName, it.patientUuid, it.prescribedDrugUuid) }, schedulersProvider.ui())
        .build()
  }
}
