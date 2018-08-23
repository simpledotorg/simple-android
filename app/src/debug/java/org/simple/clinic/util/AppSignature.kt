package org.simple.clinic.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Arrays

private const val HASH_TYPE = "SHA-256"
private const val NUM_HASHED_BYTES = 9
private const val NUM_BASE64_CHAR = 11

/**
 * This class generates app signature hash that needs
 * to be included in the SMS Message for login.
 *
 * We will generally need it only once per package name
 * and signing key, but we need it here so we can
 * generate signatures for development builds.
 */
@SuppressLint("PackageManagerGetSignatures")
class AppSignature(private val context: Context) {

  // Get all package signatures for the current package
  // For each signature create a compatible hash
  val appSignatures by lazy {
    val packageName = context.packageName
    val packageManager = context.packageManager

    val signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures

    signatures.joinToString { hash(packageName, it.toCharsString()) }
  }

  private fun hash(packageName: String, signature: String): String {
    val appInfo = "$packageName $signature"

    val messageDigest = MessageDigest.getInstance(HASH_TYPE)
    messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))

    var hashSignature = messageDigest.digest()

    // Truncated into NUM_HASHED_BYTES
    hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES)

    val base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)

    return base64Hash.substring(0, NUM_BASE64_CHAR)
  }
}
