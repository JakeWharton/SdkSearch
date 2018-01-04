####################################################################################################
####################################################################################################
########  NOTE: This file is copied from $ANDROID_HOME/tools/proguard/proguard-android.txt  ########
########                                                                                    ########
########  Changes are marked with comments prefixed with "CHANGE:"                          ########
####################################################################################################
####################################################################################################

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
#
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
# CHANGE: Not using licensing.
# CHANGE: -keep public class com.google.vending.licensing.ILicensingService
# CHANGE: -keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
# CHANGE: Keeping all getters and setters? No. Mark any reflected animation property with @Keep.
# CHANGE: -keepclassmembers public class * extends android.view.View {
# CHANGE:    void set*(***);
# CHANGE:    *** get*();
# CHANGE: }

# We want to keep methods in Activity that could be used in the XML attribute onClick
# CHANGE: The onClick attribute has been dead for years and and its use would be a bug.
# CHANGE: -keepclassmembers class * extends android.app.Activity {
# CHANGE:    public void *(android.view.View);
# CHANGE: }

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    # CHANGE: Nothing reflects on this method by default.
    # CHANGE: public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# CHANGE: No one should be reflecting on R classes.
# CHANGE: -keepclassmembers class **.R$* {
# CHANGE:     public static <fields>;
# CHANGE: }

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep

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
