import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
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

        buildConfigField("String", "API_BASE_URL1", "\"$apiUrl1\"")

        buildConfigField("String", "API_BASE_URL2", "\"$apiUrl2\"")

        buildConfigField("String", "API_BASE_URL3", "\"$apiUrl3\"")

        buildConfigField("String", "SPOTIFY_API_BASE_URL", "\"$spotifyApiUrl\"")

        buildConfigField("String", "WAVEX_API_URL", "\"$waveXApiUrl\"")
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
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
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