import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val signingProperties = Properties().apply {
    val file = File(System.getProperty("user.home"), ".local/state/seldom/signing.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}

fun signingValue(envName: String, propertyName: String): String? =
    (System.getenv(envName) ?: signingProperties.getProperty(propertyName))?.takeIf { it.isNotBlank() }

val releaseKeystore = signingValue("SELDOM_KEYSTORE", "keystore")
val releasePassword = signingValue("SELDOM_KEYSTORE_PASSWORD", "password")
val releaseAlias = signingValue("SELDOM_KEY_ALIAS", "alias")
val releaseSigningConfigured = releaseKeystore != null && releasePassword != null && releaseAlias != null

android {
    namespace = "me.kaegr.seldom"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.kaegr.seldom"
        minSdk = 30
        targetSdk = 36
        versionCode = providers.gradleProperty("seldomVersionCode").get().toInt()
        versionName = providers.gradleProperty("seldomVersionName").get()
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseKeystore!!)
                storePassword = releasePassword
                keyAlias = releaseAlias
                keyPassword = releasePassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        if (!releaseSigningConfigured) {
            throw GradleException(
                "Release signing is not configured: set SELDOM_KEYSTORE, SELDOM_KEYSTORE_PASSWORD and " +
                    "SELDOM_KEY_ALIAS, or provide keystore, password and alias in " +
                    "~/.local/state/seldom/signing.properties"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
