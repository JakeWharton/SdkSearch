-dontobfuscate

# Desugaring Java 8 language features happens after ProGuard.
-dontwarn java.lang.invoke.LambdaMetafactory

# Many libraries use JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Many libraries use Animal Sniffer to ensure they're API compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# The Android pre-handler for exceptions is loaded reflectively (via ServiceLoader).
-keep class kotlinx.coroutines.experimental.android.AndroidExceptionPreHandler { *; }

# Kotlin serialization generates sibling serializer classes which are looked up reflectively.
-keep class **.*$serializer { *; }
