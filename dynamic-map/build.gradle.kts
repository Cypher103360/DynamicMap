import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}
val getVersionName = "1.0.2"
val getArtifactId = "dynamic-map"


android {
    namespace = "com.lattice.dynamic_map"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

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
}

afterEvaluate {
    var gitUsername = System.getenv("GPR_USER")
    var gitPassword = System.getenv("GPR_TOKEN")
    if (gitUsername == null && gitPassword == null) {
        val githubProperties = Properties()
        githubProperties.load(
            rootProject.file("github.properties").inputStream()
        ) // Load GitHub credentials from github.properties file
        gitUsername = githubProperties.getProperty("gpr.usr")
        gitPassword = githubProperties.getProperty("gpr.key")
    }

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.gaurav.kumar"
                artifactId = getArtifactId
                version = getVersionName
                //artifact("$buildDir/outputs/aar/${getArtifactId}-release.aar")
            }
        }
        repositories {
            maven {
                name = "DynamicMapRepo"
                url = uri("https://maven.pkg.github.com/Cypher103360/DynamicMap")

                credentials {
                    username = gitUsername
                    password = gitPassword
                }
            }
        }
    }
}