package org.simple.clinic.drugs.selection

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenPatientPrescribedDrugsEntryBinding
import org.simple.clinic.di.injector
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
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheet
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class EditMedicinesScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), EditMedicinesUi, EditMedicinesUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: EditMedicinesEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private var binding: ScreenPatientPrescribedDrugsEntryBinding? = null

  private val toolbar
    get() = binding!!.prescribeddrugsToolbar

  private val recyclerView
    get() = binding!!.prescribeddrugsRecyclerview

  private val doneButton
    get() = binding!!.prescribeddrugsDone

  private val refillMedicineButton
    get() = binding!!.prescribeddrugsRefill

  private val groupieAdapter = GroupAdapter<ViewHolder>()

  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  private val patientUuid by unsafeLazy {
    val screenKey = screenKeyProvider.keyFor<PrescribedDrugsScreenKey>(this)
    screenKey.patientUuid
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            adapterUiEvents,
            doneClicks(),
            refillMedicineClicks())
        .compose(ReportAnalyticsEvents())
  }

  private val uiRenderer = EditMedicinesUiRenderer(this)

  private val delegate: MobiusDelegate<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect> by unsafeLazy {
    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = EditMedicinesModel.create(patientUuid),
        update = EditMedicinesUpdate(LocalDate.now(userClock), userClock.zone),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = EditMedicinesInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenPatientPrescribedDrugsEntryBinding.bind(this)

    context.injector<Injector>().inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = groupieAdapter

    val fadeAnimator = DefaultItemAnimator()
    fadeAnimator.supportsChangeAnimations = false
    recyclerView.itemAnimator = fadeAnimator
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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

  override fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewHolder>>) {
    // Replace the default fade animator with another animator that
    // plays change animations together instead of sequentially.
    if (groupieAdapter.itemCount != 0) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    val newAdapterItems = protocolDrugItems + AddNewPrescriptionListItem()

    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    newAdapterItems.forEach { it.uiEvents = adapterUiEvents }

    val hasNewItems = (groupieAdapter.itemCount == 0).not() && groupieAdapter.itemCount < newAdapterItems.size
    groupieAdapter.update(newAdapterItems)

    // Scroll to end to show newly added prescriptions.
    if (hasNewItems) {
      recyclerView.postDelayed(::scrollListToLastPosition, 300)
    }
  }

  private fun scrollListToLastPosition() {
    if (binding != null) {
      recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount - 1)
    }
  }

  override fun showNewPrescriptionEntrySheet(patientUuid: UUID) {
    activity.startActivity(CustomPrescriptionEntrySheet.intentForAddNewPrescription(context, patientUuid))
  }

  override fun goBackToPatientSummary() {
    screenRouter.pop()
  }

  override fun showDosageSelectionSheet(drugName: String, patientUuid: UUID, prescribedDrugUuid: UUID?) {
    activity.startActivity(DosagePickerSheet.intent(context, drugName, patientUuid, prescribedDrugUuid))
  }

  override fun showUpdateCustomPrescriptionSheet(prescribedDrug: PrescribedDrug) {
    activity.startActivity(CustomPrescriptionEntrySheet.intentForUpdatingPrescription(context, prescribedDrug.patientUuid, prescribedDrug.uuid))
  }

  interface Injector {
    fun inject(target: EditMedicinesScreen)
  }
}
