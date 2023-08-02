# No obfuscation because open source
# We depend on the convention of generated DAO names by Room in order to support performance profiling.
# If you decide to obfuscate the final APK, please verify the performance profiling tool continues to function.
# See: https://github.com/simpledotorg/simple-android/issues/2470
-dontobfuscate

# We process generated Room DAO metadata and use a runtime exception for performance profiling.
# If you remove this, please ensure that the profiling does not break.
# See: https://github.com/simpledotorg/simple-android/issues/2470
-keepattributes SourceFile, LineNumberTable

# Debug app only, will never be in release builds
#-dontwarn org.apache.commons.cli.CommandLineParser
-dontwarn org.apache.log4j.**
-dontwarn org.hamcrest.**
-dontwarn net.sf.saxon.**

#### OkHttp, Retrofit and Moshi
# https://github.com/square/moshi/issues/345
# https://github.com/square/moshi#proguard
-dontwarn okhttp3.**
-dontwarn retrofit2.Platform$Java8
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Enum field names are used by the integrated EnumJsonAdapter.
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
}

# The name of @JsonClass types is used to look up the generated adapter.
-keepnames @com.squareup.moshi.JsonClass class *

# Retain generated JsonAdapters if annotated type is retained.
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}

-dontwarn com.jcabi.aspects.apt.**

# Mobius
-dontwarn com.google.auto.value.AutoValue$Builder
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder

# RxJava
-dontwarn java.util.concurrent.Flow*

# GMS
-dontwarn org.checkerframework.checker.nullness.qual.PolyNull

# Firebase
-dontwarn com.google.firebase.components.*

# Protobuf
-dontwarn sun.misc.Unsafe

# Mixpanel
-dontwarn javax.servlet.http.HttpServletRequest
-dontwarn com.google.firebase.messaging.*
-dontwarn com.mixpanel.android.mpmetrics.MixpanelFCMMessagingService

# Sentry
-dontwarn javax.naming.*
-dontwarn javax.servlet.*
-dontwarn javax.servlet.http.Cookie

# iText7
-keepclassmembers enum com.itextpdf.** { *; }

# Generated Missing Rules
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.management.InstanceNotFoundException
-dontwarn javax.management.MBeanRegistrationException
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.MalformedObjectNameException
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn org.codehaus.commons.compiler.CompileException
-dontwarn org.codehaus.janino.ClassBodyEvaluator
-dontwarn sun.reflect.Reflection
-dontwarn org.codehaus.janino.ScriptEvaluator

# R8 full mode strips generic signatures from return types if not kept.
# This fix is not available in a Retrofit release yet. Once Retrofit is updated
# we can remove this
# https://github.com/square/retrofit/pull/3886
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# GSON
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Sqlite
-keep class io.requery.android.database.sqlite.SQLiteConnection {
  native <methods>;
  public <init>(...);
}

## Compose icon with reflection
-keep class androidx.compose.material.icons.filled.** { *; }
