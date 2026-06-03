import com.android.build.api.variant.FilterConfiguration

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
//    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cinemaverse.mcu"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.cinemaverse.mcu"
        minSdk = 26
        targetSdk = 37
        versionCode = 503941039
        versionName = "5.0.394.1039 Beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OMDB_API_KEY", "\"${System.getenv("OMDB_API_KEY") ?: System.getenv("VITE_OMDB_API_KEY") ?: System.getenv("NEXT_PUBLIC_OMDB_API_KEY") ?: ""}\"")
        buildConfigField("String", "TMDB_API_KEY", "\"${System.getenv("TMDB_API_KEY") ?: System.getenv("VITE_TMDB_API_KEY") ?: System.getenv("NEXT_PUBLIC_TMDB_API_KEY") ?: ""}\"")
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"${System.getenv("TMDB_READ_ACCESS_TOKEN") ?: System.getenv("VITE_TMDB_READ_ACCESS_TOKEN") ?: System.getenv("NEXT_PUBLIC_TMDB_READ_ACCESS_TOKEN") ?: ""}\"")
    }

    // Product flavors for different distribution channels
    flavorDimensions += "distribution"
    
    productFlavors {
        create("fdroid") {
            dimension = "distribution"
            applicationId = "com.cinemaverse.mcu"
            
            // F-Droid build: Enable all features (FOSS ethos)
            buildConfigField("boolean", "ENABLE_YOUTUBE_MUSIC", "true")
            buildConfigField("boolean", "ENABLE_APPLE_MUSIC", "true")
            buildConfigField("boolean", "ENABLE_DEEZER", "true")
            buildConfigField("boolean", "ENABLE_LRCLIB", "true")
            buildConfigField("boolean", "ENABLE_SPOTIFY_SEARCH", "true")
            buildConfigField("String", "FLAVOR", "\"fdroid\"")
            
            versionNameSuffix = "-fdroid"
        }
        
        create("github") {
            dimension = "distribution"
            applicationId = "com.cinemaverse.mcu"
            
            // GitHub releases: Enable all features. This is the only variant built by CI.
            buildConfigField("boolean", "ENABLE_YOUTUBE_MUSIC", "true")
            buildConfigField("boolean", "ENABLE_APPLE_MUSIC", "true")
            buildConfigField("boolean", "ENABLE_DEEZER", "true")
            buildConfigField("boolean", "ENABLE_LRCLIB", "true")
            buildConfigField("boolean", "ENABLE_SPOTIFY_SEARCH", "true")
            buildConfigField("String", "FLAVOR", "\"github\"")
            
            versionNameSuffix = "-gh"
        }
    }

    defaultConfig {
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // GitHub release artifacts are intentionally unsigned so users and downstream
            // distributors can apply their own signing keys after downloading the APK.
            signingConfig = null
//            ndk {
//                debugSymbolLevel = "SYMBOL_TABLE"
//            }
            // Reproducible builds: disable build timestamp
            if (System.getenv("CI") == "true" || System.getenv("BUILD_REPRODUCIBLE") == "true") {
                // Use a fixed timestamp for reproducible builds  
                tasks.configureEach {
                    // Disable timestamps in bundle reports for reproducible builds
                    if (name.contains("BundleReport", ignoreCase = true)) {
                        enabled = false
                    }
                }
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            //isMinifyEnabled = false
            //isDebuggable = true
            // Use the default debug signing config for installable debug artifacts.
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles
        includeInBundle = false
    }

    packaging {
        resources {
            merges += "/META-INF/INDEX.LIST"
            merges += "**/io.netty.versions.properties"
        }
    }

    // Build one universal APK by default. Set -Prhythm.enableAbiSplits=true locally if
    // per-architecture APKs are needed for testing or alternate distribution channels.
    val enableAbiSplits = providers.gradleProperty("rhythm.enableAbiSplits")
        .map(String::toBoolean)
        .getOrElse(false)

    splits {
        abi {
            isEnable = enableAbiSplits
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }

}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val abiSuffix = output.filters
                .find { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier
                ?.let { "-$it" }
                ?: ""

            output.outputFileName.set(
                "Rhythm-${android.defaultConfig.versionName}-${variant.name}${abiSuffix}.apk"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.ui.unit)
    // Desugaring library
    coreLibraryDesugaring(libs.androidx.desugar.jdk.libs)

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    
    // Material 3 dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.com.google.android.material)

    // Media3 dependencies
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.midi)
    implementation(libs.org.jellyfin.media3.ffmpeg.decoder)
    
    // Icons - Material Symbols variable font (res/font/material_symbols_outlined.ttf)
    // Replaces the deprecated material-icons-extended library for faster build times
    implementation(libs.androidx.palette.ktx)
    
    // Glance for modern widgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    
    // Physics-based animations
    implementation(libs.androidx.compose.animation)
    //noinspection GradleDependency
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.animation.core)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Permissions
    implementation(libs.com.google.accompanist.accompanist.permissions)
    
    // Fragment
    implementation(libs.androidx.fragment.ktx)
    
    // MediaRouter for Android media output switching
    implementation(libs.androidx.mediarouter)
    
    // Coil for image loading
    implementation(libs.io.coil.kt.coil.compose)
    
    // Audio metadata editing
    implementation(libs.net.jthink.jaudiotagger)
    
    // Network
    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.com.squareup.okhttp3.logging.interceptor)
    implementation(libs.com.google.code.gson.gson)
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Coroutines for async operations
    implementation(libs.org.jetbrains.kotlinx.coroutines.core)
    implementation(libs.org.jetbrains.kotlinx.coroutines.android)
    implementation(libs.androidx.foundation.layout)
    
    // WorkManager for background tasks
    implementation(libs.androidx.work.runtime.ktx)
    
    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // LeakCanary for memory leak detection (debug builds only)
    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)
}
