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
                // Url of the public repository
                //url = uri("https://maven.pkg.github.com/Cypher103360/DynamicMap")

                // Url of the private repository
                url = uri("https://maven.pkg.github.com/LatticeInnovations/dynamic-maps")


                credentials {
                    username = githubProperties.getProperty("gpr.usr")
                    password = githubProperties.getProperty("gpr.key")
                }
            }
    }
}

rootProject.name = "Dynamic Map"
include(":app")
include(":dynamic-map")
