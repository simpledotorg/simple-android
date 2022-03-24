package org.simple.clinic.drugstockreminders

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single

class DrugStockWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : RxWorker(context, workerParams) {

  init {
    ClinicApp.appComponent.inject(this)
  }
  override fun createWork(): Single<Result> {
    // todo add the work here next
  }
}
