plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.mahaesuvidha.chandrapanchangalarm"

    compileSdk = 35

    defaultConfig {
        applicationId = "com.mahaesuvidha.chandrapanchangalarm"

        minSdk = 24
        targetSdk = 35

        versionCode = 9
        versionName = "2.9"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")

    implementation("androidx.activity:activity-compose:1.10.1")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.compose.ui:ui:1.7.8")

    implementation("androidx.compose.material3:material3:1.3.1")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation(files("libs/swissephSWI-2.00.00-01.jar"))
}
