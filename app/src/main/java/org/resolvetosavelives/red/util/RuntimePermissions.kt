package org.resolvetosavelives.red.util

import android.content.Context
import android.support.v4.content.PermissionChecker

enum class RuntimePermissionResult {
  GRANTED,
  DENIED,
  NEVER_ASK_AGAIN;
}

class RuntimePermissions {

  companion object {
    fun check(context: Context, permission: String): RuntimePermissionResult {
      return parse(PermissionChecker.checkSelfPermission(context, permission))
    }

    private fun parse(grantResult: Int): RuntimePermissionResult {
      return when (grantResult) {
        PermissionChecker.PERMISSION_GRANTED -> RuntimePermissionResult.GRANTED
        PermissionChecker.PERMISSION_DENIED -> RuntimePermissionResult.DENIED
        PermissionChecker.PERMISSION_DENIED_APP_OP -> RuntimePermissionResult.NEVER_ASK_AGAIN
        else -> throw AssertionError("Unknown permission grant result: $grantResult")
      }
    }
  }
}
