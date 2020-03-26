package org.simple.clinic.security.pin

import org.simple.clinic.security.ComparisonResult
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher

class JavaHashPasswordHasher : PasswordHasher {

  override fun hash(password: String): String {
    return password.hashCode().toString()
  }

  override fun compare(hashed: String, password: String): ComparisonResult {
    val hashedPassword = hash(password)

    return if (hashed == hashedPassword) SAME else DIFFERENT
  }
}
