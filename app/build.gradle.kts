import java.util.Properties

var mapsApiKey: String? = System.getenv("MAPS_API_KEY")
if(mapsApiKey == null) {
val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
    mapsApiKey = localProperties.getProperty("MAPS_API_KEY")
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lattice.dynamicmap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lattice.dynamicmap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google maps
    implementation("com.google.maps.android:maps-compose:4.1.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps:google-maps-services:0.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("com.google.android.gms:play-services-gcm:17.0.0")


    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Permission
    implementation("com.google.accompanist:accompanist-permissions:0.19.0")

    // viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("com.google.maps.android:maps-utils-ktx:5.0.0")


    // Dynamic Map
    //implementation(project(":dynamic-map"))
    //implementation("com.gaurav.kumar:dynamic-map:1.0.2")
    implementation("com.lattice:dynamic-maps:1.0.0")
}