import java.io.FileInputStream
import java.util.Properties

var gitUsername: String? = System.getenv("GPR_USER")
var gitPassword: String? = System.getenv("GPR_TOKEN")

if (gitUsername == null && gitPassword == null) {
    val propertiesFile = File(rootProject.projectDir, "github.properties")
    val githubProperties = Properties()
    githubProperties.load(FileInputStream(propertiesFile))

    gitUsername = githubProperties.getProperty("gpr.usr")
    gitPassword = githubProperties.getProperty("gpr.key")
}

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
                    username = gitUsername
                    password = gitPassword
                }
            }
    }
}

rootProject.name = "Dynamic Map"
include(":app")
include(":dynamic-map")
