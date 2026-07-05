import java.io.FileInputStream
import java.util.Properties

// Clé API Google Books optionnelle : lue depuis local.properties (fichier NON commité,
// donc jamais visible sur GitHub). Sans clé, l'application fonctionne mais avec un
// quota de requêtes très bas et partagé avec tout le réseau WiFi (erreur 429 fréquente).
val localProperties = Properties().apply {
    val fichier = rootProject.file("local.properties")
    if (fichier.exists()) {
        load(FileInputStream(fichier))
    }
}
val cleGoogleBooks: String = localProperties.getProperty("GOOGLE_BOOKS_API_KEY", "")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // Pour la génération de code Room
}

android {
    namespace = "com.mediatheque.bdtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mediatheque.bdtracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"$cleGoogleBooks\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // --- Jetpack Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Room (base de données locale) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- Retrofit (appels réseau vers Open Library) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Coil (chargement des images de couverture) ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Tests ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
