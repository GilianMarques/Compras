import java.util.Properties
import java.io.FileInputStream


plugins {
    id("com.android.application") version "8.13.0"
    id("org.jetbrains.kotlin.android") version "2.2.20"
    id("com.google.gms.google-services") version "4.4.4"
    id("kotlin-kapt")
}


android {
    namespace = "dev.gmarques.compras"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.gmarques.compras"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropsPathFile = rootProject.file(".keystorePropsPath")

    if (!keystorePropsPathFile.exists()) {
        throw GradleException(
            "Arquivo .keystorePropsPath não encontrado na raiz do projeto.\n" +
                    "Crie o arquivo e coloque dentro o caminho absoluto do seu keystore.properties.\n" +
                    "Exemplo de conteúdo:\n" +
                    "G:/Drive/Desenvolvimento/Compras/keystore.properties"
        )
    }

    val keystorePropsPath = keystorePropsPathFile.readText().trim()
    val keystorePropertiesFile = file(keystorePropsPath)
    if (!keystorePropertiesFile.exists()) {
        throw GradleException(
            "O caminho definido em .keystorePropsPath não é válido:\n" +
                    "$keystorePropsPath\n" +
                    "Verifique se o arquivo keystore.properties existe e o caminho está correto."
        )
    }
    val keystoreProperties = Properties()
    try {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    } catch (e: Exception) {
        throw GradleException(
            "Falha ao ler o arquivo keystore.properties em:\n" +
                    "$keystorePropsPath\n" +
                    "Verifique se o arquivo está acessível e formatado corretamente.\n" +
                    "Erro original: ${e.message}"
        )
    }


    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {

        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            signingConfig = signingConfigs.getByName("release")
            resValue("string", "app_name", "Compras (Staging)")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true

    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.ericktijerou.koleton:koleton:1.0.0-beta01")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation("io.github.androidpoet:dropdown:1.1.6")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.firebase:firebase-config:23.0.1")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")



    // Testes
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")


}
