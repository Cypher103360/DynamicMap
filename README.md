# Dynamic Map Compose
![1.0.0](https://img.shields.io/badge/version-1.0.0-brightgreen)

The Dynamic Map Library is a powerful tool built on the Google Maps API, designed to simplify and enhance location-based experiences in Android applications. It seamlessly integrates with Google Maps to provide dynamic route visualization, ensuring a smooth and engaging navigation experience for users.

## Features
  - **Real-time Location Tracking:** The library automatically tracks the user's current location, updating in real-time as they move.

- **Dynamic Route Display:** Display dynamic routes between the user's current location and a specified destination. The library recalculates and updates the route in response to user interactions.

- **User-Centered Map Focus:** The map's camera is centered on the user's location, providing an intuitive and user-friendly map experience.

- **Rotating Marker:** The library includes a rotating marker that dynamically orients itself in the direction of the active route, giving users a clear sense of their navigation.

## Usage

To use the Dynamic Map Library in your Android application, follow these steps:

> Currently the GitHub Packages requires us to Authenticate to download an Android Library (Public or Private) hosted on the GitHub Package. This might change for future releases.

#### Step 1: Generate a Personal Access Token for GitHub

1. Navigate to your GitHub account:
   Settings -> Developer Settings -> Personal Access Tokens -> Generate new token

2. Select the following scopes:
   - `read:packages`

3. Generate a token and make sure to copy it. Note that you cannot see it again, so ensure to store it securely.

#### Step 2: Store your GitHub Personal Access Token details

1. Create a `github.properties` file within the root directory of your Android project.

2. For public repositories, add `github.properties` to your `.gitignore` file to keep the token private.

3. Add the following properties to `github.properties`:
   ```properties
   gpr.usr=YOUR_GITHUB_USERNAME
   gpr.key=YOUR_PERSONAL_ACCESS_TOKEN

   ```
Make sure to replace "YOUR_GITHUB_USERNAME" and "YOUR_PERSONAL_ACCESS_TOKEN" with your actual GitHub credentials.


### Installation

#### Step 1: Add Dependency

Add the following dependency to your app module's `build.gradle` file:

```gradle
dependencies {
    implementation("com.gaurav.kumar:dynamic-map:1.0.0")
}
```

#### Step 2: Configure Repository
Since this library is hosted in a private repository, you need to configure your project to access it. Add the repository URL, GitHub username, and token to your project's `settings.gradle`:

**Using `settings.gradle`:**

```kotlin
import java.io.FileInputStream
import java.util.Properties

val propertiesFile = File(rootProject.projectDir, "github.properties")
val githubProperties = Properties()
githubProperties.load(FileInputStream(propertiesFile))

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
            maven {
                name = "DynamicMapRepo"
                url = uri("https://maven.pkg.github.com/Cypher103360/DynamicMap")

                credentials {
                    username = githubProperties.getProperty("gpr.usr")
                    password = githubProperties.getProperty("gpr.key")
                }
            }
    }
}
```
- That's it! Sync your Android gradle project and the library from Github Packages should be synced with your Android Project


## How to use dynamic-map?

Before integrating the Dynamic Map Library into your Android project, take note of the following:

### Important Note: Location Permissions and Services

Ensure that your app has the necessary location permissions and that location services are enabled on the user's device. The Dynamic Map Library relies on these to provide accurate and dynamic mapping features.

By handling location permissions and enabling location services, you guarantee a seamless experience when using the Dynamic Map Library in your Android application.

**Note:** This documentation does not cover the specific implementation details of location permissions and services. You can check the current repo for the code reference.

### Google Maps API Key

To use the Dynamic Map Library, you need a valid Google Maps API Key. Ensure that you have set up the API key in the Google Cloud Console. The following specific APIs should be enabled for the library to function:

- Maps SDK for Android
- Directions API

Enabling these APIs will unlock the necessary functionality for the Dynamic Map Library.

### Integration Code Snippet

Now that you have completed the prerequisites, use the following code snippet to integrate the Dynamic Map Library into your Android project.

```kotlin
val destination = LatLng(28.52785085785891, 77.28170674108607)
val originIcon = R.drawable.origin_icon
val desIcon = R.drawable.destination
val key = BuildConfig.MAPS_API_KEY

// jetpack compose function
MapScreen(
    originIcon = originIcon,
    destinationIcon = desIcon,
    destination = destination,
    mapApiKey = key
)
```


