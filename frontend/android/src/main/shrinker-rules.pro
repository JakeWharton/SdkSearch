-dontobfuscate
-verbose

# TODO remove after figuring out what R8 bugs are blocking this from working.
-dontoptimize

# Keep annotations with RUNTIME retention and their defaults.
-keepattributes RuntimeVisible*Annotations, AnnotationDefault

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

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

# TODO This only applies when -dontoptimze is remmoved.
# R8 in full mode sees only a single subtype of this interface. As a result, it rewrites all
# references to use that subtype and removes this interface. This breaks the ServiceLoader because
# the META-INF/services/ filename does not get updated.
# TODO Remove after https://issuetracker.google.com/issues/124181030 is fixed.
# TODO determine if the -keepnames for this interface should have prevented.
-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}

# TODO This only applies when -dontoptimze is remmoved.
# R8 in full mode sees only a single subtype of this interface. As a result, it rewrites all
# references to use that subtype and removes this interface. This breaks the Retrofit Converter for
# Kotlin coroutines because it doesn't handle CompletableDeferred.
# TODO Can we automate this rule https://github.com/square/retrofit/issues/3005?
-keep class kotlinx.coroutines.Deferred {}
