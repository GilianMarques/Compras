plugins {
    id("com.android.application") version "8.10.0"
    id("org.jetbrains.kotlin.android") version "2.1.20"
    id("com.google.gms.google-services") version "4.4.2"
    id("kotlin-kapt")
}


android {
    namespace = "dev.gmarques.compras"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.gmarques.compras"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()  // Usando constante JavaVersion
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true

    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("com.ericktijerou.koleton:koleton:1.0.0-beta01")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation("io.github.androidpoet:dropdown:1.1.6")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.firebase:firebase-config:22.1.1")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")



    // Testes
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")


}