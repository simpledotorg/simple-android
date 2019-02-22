package org.simple.clinic.sync.indicator

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.widgets.setCompoundDrawableStart
import javax.inject.Inject

class SyncIndicatorView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  private val syncStatusTextView by bindView<TextView>(R.id.sync_indicator_status_text)

  @Inject
  lateinit var controller: SyncIndicatorViewController

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()

    LayoutInflater.from(context).inflate(R.layout.sync_indicator, this, true)

    TheActivity.component.inject(this)

    screenCreates()
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(SyncIndicatorViewCreated)

  fun updateState(syncState: SyncIndicatorState) {
    when (syncState) {
      ConnectToSync -> {
        syncStatusTextView.text = context.getString(R.string.sync_indicator_status_failed)
        syncStatusTextView.setCompoundDrawableStart(R.drawable.ic_warning_24dp)
      }
      is Synced -> {
        syncStatusTextView.text = context.resources.getQuantityString(R.plurals.sync_indicator_status_synced, syncState.minAgo.toInt(), syncState.minAgo)
        syncStatusTextView.setCompoundDrawableStart(R.drawable.ic_cloud_done_24dp)
      }
      SyncPending -> {
        syncStatusTextView.text = context.getString(R.string.sync_indicator_status_pending)
        syncStatusTextView.setCompoundDrawableStart(R.drawable.ic_cloud_upload_24dp)
      }
      Syncing -> {
        syncStatusTextView.text = context.getString(R.string.sync_indicator_status_syncing)
        syncStatusTextView.setCompoundDrawableStart(R.drawable.ic_sync_24dp)
      }
    }
  }
}
