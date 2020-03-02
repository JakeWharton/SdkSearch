package com.jakewharton.sdksearch.proxy

import com.google.common.truth.Truth.assertThat
import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FunctionalTest {
  @Test fun platformType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "View" }
    val expected = DocumentedType("android.view", "View", false, "https://developer.android.com/reference/kotlin/android/view/View")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun androidxType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "AppCompatButton" }
    val expected = DocumentedType("androidx.appcompat.widget", "AppCompatButton", false, "https://developer.android.com/reference/kotlin/androidx/appcompat/widget/AppCompatButton")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun androidxKotlinType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "BenchmarkState" }
    val expected = DocumentedType("androidx.benchmark", "BenchmarkState", false, "https://developer.android.com/reference/kotlin/androidx/benchmark/BenchmarkState")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun androidxComposeType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "Composable" }
    val expected = DocumentedType("androidx.compose", "Composable", false, "https://developer.android.com/reference/kotlin/androidx/compose/Composable.html")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun androidxTestType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "SdkSuppress" }
    val expected = DocumentedType("androidx.test.filters", "SdkSuppress", false, "https://developer.android.com/reference/androidx/test/filters/SdkSuppress")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun androidxConstraintLayoutType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "ConstraintLayout" }
    val expected = DocumentedType("androidx.constraintlayout.widget", "ConstraintLayout", false, "https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun materialType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "AppBarLayout" }
    val expected = DocumentedType("com.google.android.material.appbar", "AppBarLayout", false, "https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun playBillingType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "BillingClient" }
    val expected = DocumentedType("com.android.billingclient.api", "BillingClient", false, "https://developer.android.com/reference/com/android/billingclient/api/BillingClient")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun playCoreType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "SplitInstallHelper" }
    val expected = DocumentedType("com.google.android.play.core.splitinstall", "SplitInstallHelper", false, "https://developer.android.com/reference/com/google/android/play/core/splitinstall/SplitInstallHelper.html")
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun playInstallReferrerType() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "ReferrerDetails" }
    val expected = DocumentedType("com.android.installreferrer.api", "ReferrerDetails", false, "https://developer.android.com/reference/com/android/installreferrer/api/ReferrerDetails")
    assertThat(actual).isEqualTo(expected)
  }
}
