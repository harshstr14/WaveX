import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.dagger.hilt.android")
}

val localProps = gradleLocalProperties(rootDir,providers)

val apiUrl1 = localProps.getProperty("API_BASE_URL1")
    ?: error("API_URL missing in local.properties")
val apiUrl2 = localProps.getProperty("API_BASE_URL2")
    ?: error("API_URL missing in local.properties")
val apiUrl3 = localProps.getProperty("API_BASE_URL3")
    ?: error("API_URL missing in local.properties")
val spotifyApiUrl = localProps.getProperty("SPOTIFY_API_BASE_URL")
    ?: error("SPOTIFY_API_URL missing in local.properties")
val waveXApiUrl = localProps.getProperty("WAVEX_API_URL")
    ?: error("WAVEX_API_URL missing in local.properties")

val ytApiUrl = localProps.getProperty("YT_API_BASE_URL")
    ?: error("YT_API_BASE_URL missing in local.properties")

val ytStreamUrl = localProps.getProperty("YT_STREAM_URL")
    ?: error("YT_STREAM_URL missing in local.properties")

val musicAIApi = localProps.getProperty("MUSIC_AI_API_BASE_URL")
    ?: error("MUSIC_AI_API_BASE_URL missing in local.properties")

val cloudName = localProps.getProperty("CLOUD_NAME")
    ?: error("CLOUD_NAME missing in local.properties")

val apiKey = localProps.getProperty("API_KEY")
    ?: error("API_KEY missing in local.properties")

val apiSecret = localProps.getProperty("API_SECRET")
    ?: error("API_SECRET missing in local.properties")

android {
    namespace = "com.example.wavex"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.wavex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.151"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL1", "\"${apiUrl1.removeSurrounding("\"")}\"")

        buildConfigField("String", "API_BASE_URL2", "\"${apiUrl2.removeSurrounding("\"")}\"")

        buildConfigField("String", "API_BASE_URL3", "\"${apiUrl3.removeSurrounding("\"")}\"")

        buildConfigField("String", "SPOTIFY_API_BASE_URL", "\"${spotifyApiUrl.removeSurrounding("\"")}\"")

        buildConfigField("String", "WAVEX_API_URL", "\"${waveXApiUrl.removeSurrounding("\"")}\"")

        buildConfigField("String", "YT_API_BASE_URL", "\"${ytApiUrl.removeSurrounding("\"")}\"")

        buildConfigField("String", "YT_STREAM_URL", "\"${ytStreamUrl.removeSurrounding("\"")}\"")

        buildConfigField("String", "MUSIC_AI_API_BASE_URL", "\"${musicAIApi.removeSurrounding("\"")}\"")

        buildConfigField("String", "CLOUD_NAME", "\"${cloudName.removeSurrounding("\"")}\"")
        buildConfigField("String", "API_KEY", "\"${apiKey.removeSurrounding("\"")}\"")
        buildConfigField("String", "API_SECRET", "\"${apiSecret.removeSurrounding("\"")}\"")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

configurations.configureEach {
    exclude(
        group = "com.google.firebase",
        module = "protolite-well-known-types"
    )
}

dependencies {
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.newpipeextractor)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.config)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.runtime.livedata)
    ksp(libs.androidx.room.compiler)
    implementation(libs.pratikfagadiya.animatedsmoothbottomnavigation.jetpackcompose)
    implementation(libs.ucrop)
    implementation(libs.cloudinary.android)
    implementation(libs.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.media)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.coil.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.lottie.compose)
}