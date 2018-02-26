-dontobfuscate

# Desugaring Java 8 language features happens after ProGuard.
-dontwarn java.lang.invoke.LambdaMetafactory

# Many libraries use JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Many libraries use Animal Sniffer to ensure they're API compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Kotlin serialization generates sibling serializer classes which are looked up reflectively.
-keep class **.*$serializer { *; }

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
