plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.rust.android)
}

android {
    namespace = "com.brunoshiroma.vibememory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brunoshiroma.vibememory"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // No dedicated release keystore is configured for this sideload-only
            // benchmark app, so release APKs are signed with the debug key. This
            // keeps `./gradlew assembleRelease` (used by the tag release workflow)
            // producing installable APKs without requiring secrets.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Produces one APK per ABI (instead of a single fat APK bundling all four
    // native library sets) so the release workflow can publish arm64, arm,
    // x86, and x64 builds separately.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

// Cross-compiles the JNI bridge crate (rust/memory-bench-jni), which in turn
// depends on the memory-cache-bench crate from
// https://github.com/brunoshiroma/rust-vibe-memory-test, into a native
// library for each supported Android ABI.
cargo {
    module = "../rust/memory-bench-jni"
    libname = "memory_bench_jni"
    targets = listOf("arm", "arm64", "x86", "x86_64")
    prebuiltToolchains = true
}

tasks.matching { it.name.matches(Regex("merge.*JniLibFolders")) }.configureEach {
    dependsOn("cargoBuild")
}

tasks.named("clean") {
    dependsOn("cargoClean")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
