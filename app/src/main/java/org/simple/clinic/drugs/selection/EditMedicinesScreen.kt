package org.simple.clinic.drugs.selection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListPrescribeddrugsCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsNewCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsProtocolDrugBinding
import org.simple.clinic.databinding.ScreenPatientPrescribedDrugsEntryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.AddNewPrescriptionClicked
import org.simple.clinic.drugs.CustomPrescriptionClicked
import org.simple.clinic.drugs.EditMedicinesEffect
import org.simple.clinic.drugs.EditMedicinesEffectHandler
import org.simple.clinic.drugs.EditMedicinesEvent
import org.simple.clinic.drugs.EditMedicinesInit
import org.simple.clinic.drugs.EditMedicinesModel
import org.simple.clinic.drugs.EditMedicinesUiRenderer
import org.simple.clinic.drugs.EditMedicinesUpdate
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescribedDrugsDoneClicked
import org.simple.clinic.drugs.PresribedDrugsRefillClicked
import org.simple.clinic.drugs.ProtocolDrugClicked
import org.simple.clinic.drugs.search.DrugsSearchScreen
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheet
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class EditMedicinesScreen :
    BaseScreen<
        PrescribedDrugsScreenKey,
        ScreenPatientPrescribedDrugsEntryBinding,
        EditMedicinesModel,
        EditMedicinesEvent,
        EditMedicinesEffect,
        Unit>(), EditMedicinesUi, EditMedicinesUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: EditMedicinesEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var features: Features

  private val toolbar
    get() = binding.prescribeddrugsToolbar

  private val recyclerView
    get() = binding.prescribeddrugsRecyclerview

  private val doneButton
    get() = binding.prescribeddrugsDone

  private val refillMedicineButton
    get() = binding.prescribeddrugsRefill

  private val adapter = ItemAdapter(
      diffCallback = DrugListItem.Differ(),
      bindings = mapOf(
          R.layout.list_prescribeddrugs_protocol_drug to { layoutInflater, parent ->
            ListPrescribeddrugsProtocolDrugBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_prescribeddrugs_custom_drug to { layoutInflater, parent ->
            ListPrescribeddrugsCustomDrugBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_prescribeddrugs_new_custom_drug to { layoutInflater, parent ->
            ListPrescribeddrugsNewCustomDrugBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val patientUuid by unsafeLazy {
    screenKey.patientUuid
  }

  override fun defaultModel() = EditMedicinesModel.create(patientUuid = patientUuid)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenPatientPrescribedDrugsEntryBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = EditMedicinesUiRenderer(this)

  override fun events() = Observable
      .mergeArray(
          protocolDrugClicks(),
          customDrugClicks(),
          addNewCustomDrugClicks(),
          doneClicks(),
          refillMedicineClicks())
      .compose(ReportAnalyticsEvents())
      .cast<EditMedicinesEvent>()

  override fun createUpdate() = EditMedicinesUpdate(LocalDate.now(userClock), userClock.zone)

  override fun createInit() = EditMedicinesInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener { router.pop() }
    recyclerView.setHasFixedSize(false)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter

    val fadeAnimator = DefaultItemAnimator()
    fadeAnimator.supportsChangeAnimations = false
    recyclerView.itemAnimator = fadeAnimator
  }

  private fun doneClicks() = doneButton.clicks().map { PrescribedDrugsDoneClicked }

  private fun refillMedicineClicks() = refillMedicineButton.clicks().map { PresribedDrugsRefillClicked }

  override fun showRefillMedicineButton() {
    refillMedicineButton.visibility = VISIBLE
  }

  override fun showDoneButton() {
    doneButton.visibility = VISIBLE
  }

  override fun hideRefillMedicineButton() {
    refillMedicineButton.visibility = GONE
  }

  override fun hideDoneButton() {
    doneButton.visibility = GONE
  }

  override fun populateDrugsList(protocolDrugItems: List<DrugListItem>) {
    // Replace the default fade animator with another animator that
    // plays change animations together instead of sequentially.
    if (adapter.itemCount != 0) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    val newAdapterItems = protocolDrugItems + AddNewPrescriptionListItem

    val hasNewItems = (adapter.itemCount == 0).not() && adapter.itemCount < newAdapterItems.size
    adapter.submitList(newAdapterItems)

    // Scroll to end to show newly added prescriptions.
    if (hasNewItems) {
      recyclerView.postDelayed(::scrollListToLastPosition, 300)
    }
  }

  private fun scrollListToLastPosition() {
    recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount - 1)
  }

  override fun showNewPrescriptionEntrySheet(patientUuid: UUID) {
    if (features.isEnabled(Feature.CustomDrugSearchScreen)) {
      router.push(DrugsSearchScreen.Key(patientId = patientUuid))
    } else {
      activity.startActivity(CustomPrescriptionEntrySheet.intentForAddNewPrescription(requireContext(), patientUuid))
    }
  }

  override fun goBackToPatientSummary() {
    router.pop()
  }

  override fun showDosageSelectionSheet(
      drugName: String,
      patientUuid: UUID,
      prescribedDrugUuid: UUID?
  ) {
    activity.startActivity(DosagePickerSheet.intent(requireContext(), drugName, patientUuid, prescribedDrugUuid))
  }

  override fun showUpdateCustomPrescriptionSheet(prescribedDrug: PrescribedDrug) {
    activity.startActivity(CustomPrescriptionEntrySheet.intentForUpdatingPrescription(requireContext(), prescribedDrug.patientUuid, prescribedDrug.uuid))
  }

  private fun protocolDrugClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<DrugListItemClicked.PrescribedDrugClicked>()
        .map { ProtocolDrugClicked(it.drugName, it.prescribedDrug) }
  }

  private fun customDrugClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<DrugListItemClicked.CustomPrescriptionClicked>()
        .map { CustomPrescriptionClicked(it.prescribedDrug) }
  }

  private fun addNewCustomDrugClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<DrugListItemClicked.AddNewPrescriptionClicked>()
        .map { AddNewPrescriptionClicked }
  }

  interface Injector {
    fun inject(target: EditMedicinesScreen)
  }
}
