package org.simple.clinic.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays

/**
 * This is a helper class to generate your message hash to be included in your SMS message.
 *
 *
 * Without the correct hash, your app won't receive the message callback. This only needs to be
 * generated once per app and stored. Then you can remove this helper class from your code.
 */
private const val HASH_TYPE = "SHA-256"
private const val NUM_HASHED_BYTES = 9
private const val NUM_BASE64_CHAR = 11

@SuppressLint("PackageManagerGetSignatures")
class AppSignatureHelper(private val context: Context) {

  // Get all package signatures for the current package
  // For each signature create a compatible hash
  val appSignatures by lazy {
    try {
      val packageName = context.packageName
      val packageManager = context.packageManager
      val signatures = packageManager.getPackageInfo(packageName,
          PackageManager.GET_SIGNATURES).signatures
      signatures.joinToString { hash(packageName, it.toCharsString()) }
    } catch (e: PackageManager.NameNotFoundException) {
      Timber.e(e, "Unable to find package to obtain hash.")
      ""
    }
  }

  private fun hash(packageName: String, signature: String): String {
    val appInfo = "$packageName $signature"
    try {
      val messageDigest = MessageDigest.getInstance(HASH_TYPE)
      messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
      var hashSignature = messageDigest.digest()

      // truncated into NUM_HASHED_BYTES
      hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES)
      // encode into Base64
      var base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
      base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR)

      Timber.d("pkg: $packageName -- hash: $base64Hash")
      return base64Hash
    } catch (e: NoSuchAlgorithmException) {
      Timber.e(e, "hash:NoSuchAlgorithm")
    }

    return ""
  }
}
