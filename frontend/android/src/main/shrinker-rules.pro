-dontoptimize
-dontpreverify
-dontobfuscate
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# Enum.valueOf(Class, String) and others invoke this method reflectively.
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
}

# Parcelable CREATOR fields are looked up reflectively when de-parceling.
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# For support library @Keep annotation. Remove once embedded rules ship.
-keep @android.support.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}

# Retrofit does reflection on generic parameters and InnerClass is required to use Signature.
-keepattributes Signature, InnerClasses

# Many libraries use JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Many libraries use Animal Sniffer to ensure they're API compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Kotlin serialization generates sibling serializer classes which are looked up reflectively by a
# function on the Companion object.
-keep class **.*$serializer { *; }
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
