package org.simple.clinic.illustration

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import org.simple.clinic.storage.files.FileOperations
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import retrofit2.Call
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class IllustrationSync @Inject constructor(
    private val illustrations: List<HomescreenIllustration>,
    private val fileDownloadService: FileDownloadService,
    private val fileOperations: FileOperations,
    @Named("sync_config_daily") private val configProvider: Single<SyncConfig>,
    @Named("homescreen-illustration-folder") private val illustrationsFolder: File
) : ModelSync {

  override fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable = Completable.fromAction {
    illustrations.forEach { illustration ->
      saveIllustration(illustration)
    }
  }

  override fun syncConfig() = configProvider

  private fun saveIllustration(illustration: HomescreenIllustration) {
    val illustrationsFile = File(illustrationsFolder, illustration.eventId)
    if (illustrationsFile.exists()) return

    fileDownloadService.downloadFile(illustration.illustrationUrl)
        .enqueue(object : retrofit2.Callback<ResponseBody> {
          override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // TODO Make analytics call
          }

          override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            response.body()?.byteStream()?.use { inputStream ->
              fileOperations.createFileIfItDoesNotExist(illustrationsFile)
              illustrationsFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
              }
            }
          }
        })
  }
}
