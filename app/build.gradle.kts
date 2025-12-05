import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.compose)
}

// =============================================================================
// Configuration Validation
// =============================================================================
// Check for required configuration files and provide helpful error messages

val googleServicesFile = file("google-services.json")
if (!googleServicesFile.exists()) {
    logger.warn("""
        |
        |╔══════════════════════════════════════════════════════════════════════════════╗
        |║  MISSING: google-services.json                                            ║
        |╠══════════════════════════════════════════════════════════════════════════════╣
        |║  The app/google-services.json file is required but not found.                ║
        |║                                                                              ║
        |║  To fix this:                                                                ║
        |║  1. Go to https://console.firebase.google.com/                               ║
        |║  2. Create/select your Firebase project                                      ║
        |║  3. Add Android app with package: com.domicoder.miunieventos                 ║
        |║  4. Download google-services.json                                            ║
        |║  5. Place it in the app/ directory                                           ║
        |║                                                                              ║
        |║  See SETUP.md for detailed instructions                                   ║
        |╚══════════════════════════════════════════════════════════════════════════════╝
        |
    """.trimMargin())
}

android {
    namespace = "com.domicoder.miunieventos"
    compileSdk = 34

    defaultConfig {
        manifestPlaceholders += mapOf()
        applicationId = "com.domicoder.miunieventos"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load API keys from local.properties
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        } else {
            logger.warn("""
                |
                |╔══════════════════════════════════════════════════════════════════════════════╗
                |║  MISSING: local.properties                                                ║
                |╠══════════════════════════════════════════════════════════════════════════════╣
                |║  Create local.properties from the template:                                  ║
                |║    cp local.properties.template local.properties                             ║
                |║                                                                              ║
                |║  Then add your MAPS_API_KEY to enable Google Maps functionality.             ║
                |║                                                                              ║
                |║  See SETUP.md for detailed instructions                                   ║
                |╚══════════════════════════════════════════════════════════════════════════════╝
                |
            """.trimMargin())
        }
        
        val mapsApiKey = properties.getProperty("MAPS_API_KEY", "")
        if (mapsApiKey.isEmpty() || mapsApiKey == "your_maps_api_key_here" || mapsApiKey == "your_actual_maps_api_key_here") {
            logger.warn("""
                |
                |╔══════════════════════════════════════════════════════════════════════════════╗
                |║  MAPS_API_KEY not configured                                              ║
                |╠══════════════════════════════════════════════════════════════════════════════╣
                |║  Google Maps will not work without a valid API key.                          ║
                |║                                                                              ║
                |║  To fix this:                                                                ║
                |║  1. Go to https://console.cloud.google.com/apis/credentials                  ║
                |║  2. Create an API key                                                        ║
                |║  3. Enable "Maps SDK for Android"                                            ║
                |║  4. Add the key to local.properties: MAPS_API_KEY=your_key_here              ║
                |║                                                                              ║
                |║  See SETUP.md for detailed instructions                                   ║
                |╚══════════════════════════════════════════════════════════════════════════════╝
                |
            """.trimMargin())
        }
        
        buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    tasks.register("copyGoogleServicesToAssets", Copy::class) {
        val assetsDir = file("src/main/assets")
        assetsDir.mkdirs()
        from("google-services.json")
        into(assetsDir)
        include("google-services.json")
        onlyIf { file("google-services.json").exists() }
    }
    
    tasks.named("preBuild") {
        dependsOn("copyGoogleServicesToAssets")
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    
    // Google Sign-In
    implementation(libs.google.auth)

    
    // Maps
    implementation(libs.google.maps)
    implementation(libs.maps.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // QR Code
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
