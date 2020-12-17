package org.simple.clinic.editpatient.deletepatient

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListDeleteReasonBinding
import org.simple.clinic.databinding.ScreenDeletePatientBinding
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.dp
import javax.inject.Inject

class DeletePatientScreen(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), UiActions, DeletePatientUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: DeletePatientEffectHandler.Factory

  private val viewRenderer = DeletePatientViewRenderer(this)

  private val screenKey by unsafeLazy {
    screenRouter.key<DeletePatientScreenKey>(this)
  }

  private var binding: ScreenDeletePatientBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val deleteReasonsRecyclerView
    get() = binding!!.deleteReasonsRecyclerView

  private val deleteReasonsAdapter = ItemAdapter(
      diffCallback = DeleteReasonItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_delete_reason to { layoutInflater, parent ->
            ListDeleteReasonBinding.inflate(layoutInflater, parent, false)
          }
      )
  )
  private val dialogEvents = PublishSubject.create<DeletePatientEvent>()
  private val events: Observable<DeletePatientEvent> by unsafeLazy {
    Observable
        .mergeArray(
            dialogEvents,
            adapterEvents()
        )
        .compose(ReportAnalyticsEvents())
        .cast<DeletePatientEvent>()
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = events,
        defaultModel = DeletePatientModel.default(screenKey.patientUuid),
        init = DeletePatientInit(),
        update = DeletePatientUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = viewRenderer::render
    )
  }

  private val deleteConfirmationDialog by unsafeLazy {
    AlertDialog.Builder(context, R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.deletereason_confirm_title)
        .setNegativeButton(R.string.deletereason_confirm_negative, null)
        .create()
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return

    binding = ScreenDeletePatientBinding.bind(this)

    context.injector<DeletePatientScreenInjector>().inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }
    with(deleteReasonsRecyclerView) {
      adapter = deleteReasonsAdapter
      addItemDecoration(DividerItemDecorator(context, marginStart = 56.dp, marginEnd = 16.dp))
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    deleteConfirmationDialog.dismiss()
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val viewState = delegate.onRestoreInstanceState(state)
    super.onRestoreInstanceState(viewState)
  }

  override fun showDeleteReasons(patientDeleteReasons: List<PatientDeleteReason>, selectedReason: PatientDeleteReason?) {
    deleteReasonsAdapter.submitList(DeleteReasonItem.from(patientDeleteReasons, selectedReason))
  }

  override fun showConfirmDeleteDialog(patientName: String, deletedReason: DeletedReason) {
    val message = context.getString(R.string.deletereason_confirm_message, patientName)

    with(deleteConfirmationDialog) {
      setMessage(message)
      setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.deletereason_confirm_positive)) { _, _ ->
        dialogEvents.onNext(ConfirmPatientDeleteClicked(deletedReason))
      }
      show()
    }
  }

  override fun showConfirmDiedDialog(patientName: String) {
    val message = context.getString(R.string.deletereason_confirm_message, patientName)

    with(deleteConfirmationDialog) {
      setMessage(message)
      setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.deletereason_confirm_positive)) { _, _ ->
        dialogEvents.onNext(ConfirmPatientDiedClicked)
      }
      show()
    }
  }

  override fun showHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, RouterDirection.BACKWARD)
  }

  private fun adapterEvents(): Observable<DeletePatientEvent> {
    return deleteReasonsAdapter
        .itemEvents
        .ofType<DeleteReasonItem.Event.Clicked>()
        .map { PatientDeleteReasonClicked(it.reason) }
  }
}
