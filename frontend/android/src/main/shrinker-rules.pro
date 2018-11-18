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

# Kotlin serialization generates sibling serializer classes which are looked up reflectively by a
# function on the Companion object.
-keep class **.*$serializer { *; }
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
