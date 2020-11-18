package org.simple.clinic.bloodsugar.unitselection

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_bloodsugar_selectionunit.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import javax.inject.Inject

class BloodSugarUnitSelectionDialog : AppCompatDialogFragment(), BloodSugarUnitSelectionUiActions {

  private lateinit var layout: View

  @Inject
  lateinit var effectHandlerFactory: BloodSugarUnitSelectionEffectHandler.Factory

  companion object {

    private const val FRAGMENT_TAG = "blood_sugar_unit_selection_tag"
    private const val KEY_UNIT_PREF = "bloodSugarUnitSelection"

    fun show(fragmentManager: FragmentManager, bloodSugarUnitPreference: BloodSugarUnitPreference) {
      val existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val arguments = Bundle()
      arguments.putSerializable(KEY_UNIT_PREF, bloodSugarUnitPreference)

      val fragment = BloodSugarUnitSelectionDialog()
      fragment.arguments = arguments


      fragmentManager
          .beginTransaction()
          .add(fragment, FRAGMENT_TAG)
          .commitNowAllowingStateLoss()
    }
  }

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val dialogEvents = PublishSubject.create<BloodSugarUnitSelectionEvent>()
  private val events by unsafeLazy {
    val doneButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    doneClicks(doneButton)
  }

  private fun doneClicks(doneButton: Button): Observable<BloodSugarUnitSelectionEvent> {

    val radioIdToBloodSugarUnits = mapOf(
        R.id.bloodSugarUnitMg to BloodSugarUnitPreference.Mg,
        R.id.bloodSugarUnitMmol to BloodSugarUnitPreference.Mmol
    )

    return doneButton
        .clicks()
        .map {
          val bloodSugarUnitSelectionValue = radioIdToBloodSugarUnits.getValue(bloodSugarUnitGroup.checkedRadioButtonId)
          DoneClicked(bloodSugarUnitSelection = bloodSugarUnitSelectionValue)
        }
  }

  private val delegate: MobiusDelegate<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEvent, BloodSugarUnitSelectionEffect> by unsafeLazy {
    val bloodSugarUnitPreference = arguments!!.getSerializable(KEY_UNIT_PREF)

    MobiusDelegate.forActivity(
        events = dialogEvents.ofType(),
        defaultModel = BloodSugarUnitSelectionModel.create(bloodSugarUnitPreference = bloodSugarUnitPreference as BloodSugarUnitPreference),
        update = BloodSugarUnitSelectionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<BloodSugarUnitSelectionDialogInjector>().inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  @SuppressLint("CheckResult", "InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    layout = LayoutInflater.from(context).inflate(R.layout.dialog_bloodsugar_selectionunit, null)
    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.blood_sugar_unit_selection_choose)
        .setView(layout)
        .setPositiveButton(R.string.blood_sugar_unit_selection_done, null)
        .create()
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    super.onStop()
    delegate.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  @SuppressLint("CheckResult")
  override fun onResume() {
    super.onResume()
    events
        .takeUntil(screenDestroys)
        .subscribe(dialogEvents::onNext)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun closeDialog() {
    dismiss()
  }

  override fun prefillBloodSugarUnitSelection(blodSugarUnitPreference: BloodSugarUnitPreference) {
    when (blodSugarUnitPreference) {
      BloodSugarUnitPreference.Mg -> bloodSugarUnitGroup.check(R.id.bloodSugarUnitMg)
      BloodSugarUnitPreference.Mmol -> bloodSugarUnitGroup.check(R.id.bloodSugarUnitMmol)
    }
  }

  interface BloodSugarUnitSelectionDialogInjector {
    fun inject(target: BloodSugarUnitSelectionDialog)
  }
}
