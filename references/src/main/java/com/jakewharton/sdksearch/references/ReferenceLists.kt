package com.jakewharton.sdksearch.references

import com.jakewharton.sdksearch.references.SourceProject.BASE
import com.jakewharton.sdksearch.references.SourceProject.CONSTRAINT_LAYOUT
import com.jakewharton.sdksearch.references.SourceProject.DATABINDING
import com.jakewharton.sdksearch.references.SourceProject.ICU
import com.jakewharton.sdksearch.references.SourceProject.LIBCORE
import com.jakewharton.sdksearch.references.SourceProject.MULTIDEX
import com.jakewharton.sdksearch.references.SourceProject.SUPPORT
import com.jakewharton.sdksearch.references.SourceProject.VOIP

const val PRODUCTION_DAC = "https://developer.android.com/"
const val PRODUCTION_GITWEB = "https://android.googlesource.com/"

val REFERENCE_LISTS = mapOf(
    "platform" to "reference/lists.js",
    "support" to "reference/android/support/lists.js",
    "wear" to "reference/android/support/wearable/lists.js",
    "test" to "reference/android/support/test/lists.js",
    "constraint" to "reference/android/support/constraint/lists.js",
    "arch" to "reference/android/arch/lists.js"
)

enum class SourceProject(val projectDir: String) {
  LIBCORE("platform/libcore/"),
  BASE("platform/frameworks/base/"),
  VOIP("platform/frameworks/opt/net/voip/"),
  SUPPORT("platform/frameworks/support/"),
  MULTIDEX("platform/frameworks/multidex/"),
  CONSTRAINT_LAYOUT("platform/frameworks/opt/sherpa/"),
  ICU("platform/external/icu/"),
  DATABINDING("platform/frameworks/data-binding/"),
  ;

  init {
    check(projectDir.endsWith('/'))
  }
}
data class SourceLocation(val project: SourceProject, val baseDir: String) {
  init {
    check(baseDir.endsWith('/'))
  }
}

val PACKAGE_SOURCE_MAP = mapOf(
    "java" to SourceLocation(LIBCORE, "ojluni/src/main/java/"),
    "javax" to SourceLocation(LIBCORE, "ojluni/src/main/java/"),
    "javax.microeition" to null,
    "org" to SourceLocation(LIBCORE, "luni/src/main/java/"),
    "org.json" to null,
    "org.xmlpull" to null,
    "org.apache.http" to SourceLocation(LIBCORE, "core/java/"),
    "java.math" to SourceLocation(LIBCORE, "luni/src/main/java/"),
    "android" to SourceLocation(BASE, "core/java/"),
    "android.databinding" to SourceLocation(DATABINDING, "extensions/library/src/main/java/"),
    "android.databinding.Bindable" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.BindingAdapter" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.BindingConversion" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.BindingMethod" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.BindingMethods" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.CallbackRegistry" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.InverseBindingAdapter" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.InverseBindingListener" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.InverseBindingMethod" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.InverseBindingMethods" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.Observable" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.ObservableList" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.databinding.ObservableMap" to SourceLocation(DATABINDING, "baseLibrary/src/main/java/"),
    "android.drm" to SourceLocation(BASE, "drm/java/"),
    "android.drm.mobile1" to SourceLocation(BASE, "media/java/"),
    "android.renderscript" to SourceLocation(BASE, "rs/java/"),
    "android.graphics" to SourceLocation(BASE, "graphics/java/"),
    "android.icu" to SourceLocation(ICU, "android_icu4j/src/main/java/"),
    "android.security" to SourceLocation(BASE, "keystore/java/"),
    "android.system" to SourceLocation(LIBCORE, "luni/src/main/java/"),
    "android.location" to SourceLocation(BASE, "location/java/"),
    "android.media" to SourceLocation(BASE, "media/java/"),
    "android.media.effect" to SourceLocation(BASE, "media/mca/effect/java/"),
    "android.mtp" to SourceLocation(BASE, "media/java/"),
    "android.opengl" to SourceLocation(BASE, "opengl/java/"),
    "android.sax" to SourceLocation(BASE, "sax/java/"),
    "android.telecom" to SourceLocation(BASE, "telecomm/java/"),
    "android.telephony" to SourceLocation(BASE, "telephony/java/"),
    "android.net.rtp" to SourceLocation(VOIP, "src/java/"),
    "android.net.sip" to SourceLocation(VOIP, "src/java/"),
    "android.net.wifi" to SourceLocation(BASE, "wifi/java/"),
    "android.support.animation" to SourceLocation(SUPPORT, "dynamic-animation/src/main/java/"),
    "android.support.annotation" to SourceLocation(SUPPORT, "annotations/src/main/java/"),
    "android.support.app.recommendation" to SourceLocation(SUPPORT, "recommendation/src/main/java/"),
    "android.support.compat" to null,
    "android.support.content" to SourceLocation(SUPPORT, "content/src/main/java/"),
    "android.support.coreui" to null,
    "android.support.coreutils" to null,
    "android.support.customtabs" to SourceLocation(SUPPORT, "customtabs/src/main/java/"),
    "android.support.design" to SourceLocation(SUPPORT, "design/src/"),
    "android.support.fragment" to null,
    "android.support.graphics.drawable" to SourceLocation(SUPPORT, "graphics/drawable/static/src/main/java/"),
    "android.support.multidex" to SourceLocation(MULTIDEX, "library/src/"),
    "android.support.media" to SourceLocation(SUPPORT, "exifinterface/src/main/java/"),
    "android.support.media.tv" to SourceLocation(SUPPORT, "tv-provider/src/main/java/"),
    "android.support.mediacompat" to null,
    "android.support.mediacompat.testlib" to SourceLocation(SUPPORT, "media-compat-test-lib/src/main/java/"),
    "android.support.percent" to SourceLocation(SUPPORT, "percent/src/main/java/"),
    "android.support.text.emoji" to SourceLocation(SUPPORT, "emoji/core/src/main/java/"),
    "android.support.text.emoji.bundled" to SourceLocation(SUPPORT, "emoji/bundled/src/main/java/"),
    "android.support.text.emoji.widget" to SourceLocation(SUPPORT, "emoji/appcompat/src/main/java/"),
    "android.support.transition" to SourceLocation(SUPPORT, "transition/src/"),
    "android.support.v4" to SourceLocation(SUPPORT, "compat/src/main/java/"),
    "android.support.v4.content" to SourceLocation(SUPPORT, "core-utils/src/main/java/"),
    "android.support.v4.math" to SourceLocation(SUPPORT, "core-utils/src/main/java/"),
    "android.support.v4.media" to SourceLocation(SUPPORT, "media-compat/java/"),
    "android.support.v4.print" to SourceLocation(SUPPORT, "core-utils/src/main/java/"),
    "android.support.v4.provider" to SourceLocation(SUPPORT, "core-utils/src/main/java/"),
    "android.support.v4.view.animation" to SourceLocation(SUPPORT, "core-ui/src/main/java/"),
    "android.support.v4.widget" to SourceLocation(SUPPORT, "core-ui/src/main/java/"),
    "android.support.v7" to SourceLocation(SUPPORT, "compat/src/main/java/"),
    "android.support.v7.app" to SourceLocation(SUPPORT, "v7/appcompat/src/main/java/"),
    "android.support.v7.content.res" to SourceLocation(SUPPORT, "v7/appcompat/src/main/java/"),
    "android.support.v7.graphics" to SourceLocation(SUPPORT, "v7/palette/src/main/java/"),
    "android.support.v7.graphics.drawable" to SourceLocation(SUPPORT, "v7/appcompat/src/main/java/"),
    "android.support.v7.media" to SourceLocation(SUPPORT, "v7/mediarouter/src/"),
    "android.support.v7.preference" to SourceLocation(SUPPORT, "v7/preference/src/main/java/"),
    "android.support.v7.recyclerview.extensions" to SourceLocation(SUPPORT, "paging/runtime/src/main/java/"),
    "android.support.v7.util" to SourceLocation(SUPPORT, "v7/recyclerview/src/main/java/"),
    "android.support.v7.view" to SourceLocation(SUPPORT, "v7/appcompat/src/main/java/"),
    "android.support.v7.widget" to SourceLocation(SUPPORT, "v7/appcompat/src/main/java/"),
    "android.support.v7.widget.helper" to SourceLocation(SUPPORT, "v7/recyclerview/src/main/java/"),
    "android.support.v7.widget.util" to SourceLocation(SUPPORT, "v7/recyclerview/src/main/java/"),
    "android.support.v8.renderscript" to SourceLocation(SUPPORT, "v8/renderscript/java/src/"),
    "android.support.v13" to SourceLocation(SUPPORT, "v13/java/"),
    "android.support.v14.preference" to SourceLocation(SUPPORT, "v14/preference/src/main/java/"),
    "android.support.v17.leanback" to SourceLocation(SUPPORT, "v17/leanback/src/"),
    "android.support.v17.preference" to SourceLocation(SUPPORT, "v17/preference-leanback/src/"),
    "android.support.wear" to SourceLocation(SUPPORT, "wear/src/main/java/"),
    "android.support.constraint" to SourceLocation(CONSTRAINT_LAYOUT, "constraintlayout/src/main/java/"),
    "android.arch.lifecycle" to SourceLocation(SUPPORT, "lifecycle/extensions/src/main/java/"),
    "android.arch.paging" to SourceLocation(SUPPORT, "paging/common/src/main/java/"),
    "android.arch.persistence.db" to SourceLocation(SUPPORT, "room/db/src/main/java/"),
    "android.arch.persistence.db.framework" to SourceLocation(SUPPORT, "room/db-impl/src/main/java/"),
    "android.arch.persistence.room" to SourceLocation(SUPPORT, "room/common/src/main/java/"),
    "android.arch.persistence.room.migration" to SourceLocation(SUPPORT, "room/runtime/src/main/java/"),
    "android.arch.persistence.room.testing" to SourceLocation(SUPPORT, "room/testing/src/main/java/")
)

fun sourceUrl(packageName: String, className: String): String? {
  val topLevelClassName = className.substringBefore('.')

  var lookup = "$packageName.$topLevelClassName"
  while (true) {
    if (PACKAGE_SOURCE_MAP.containsKey(lookup)) {
      val path = PACKAGE_SOURCE_MAP[lookup] ?: return null // Explicitly absent.
      return buildString {
        append(PRODUCTION_GITWEB)
        append(path.project.projectDir)
        append("+/refs/heads/master/")
        append(path.baseDir)
        append(packageName.replace('.', '/'))
        append('/')
        append(topLevelClassName)
        append(".java")
      }
    }
    val lastDot = lookup.lastIndexOf('.')
    if (lastDot == -1) {
      return null // No entry.
    }
    lookup = lookup.substring(0, lastDot)
  }
}
