# No obfuscation because open source
-dontobfuscate

# Debug app only, will never be in release builds
#-dontwarn com.facebook.stetho.**
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
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}
-keepnames @com.squareup.moshi.JsonClass class *

-dontwarn com.jcabi.aspects.apt.**
