package org.simple.clinic.security.pin

import io.reactivex.Single
import org.simple.clinic.security.ComparisonResult
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher

class JavaHashPasswordHasher : PasswordHasher {

  override fun hash(password: String): Single<String> {
    return Single.just(genHash(password))
  }

  override fun compare(hashed: String, password: String): Single<ComparisonResult> {
    val hashedPassword = genHash(password)

    val result = if (hashed == hashedPassword) SAME else DIFFERENT
    return Single.just(result)
  }

  private fun genHash(string: String): String = string.hashCode().toString()
}
