package org.simple.clinic.security

interface PasswordHasher {

  fun hash(password: String): String

  fun compare(hashed: String, password: String): ComparisonResult
}
