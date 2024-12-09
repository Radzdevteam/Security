# Radz App Security
[![](https://jitpack.io/v/Radzdevteam/Security.svg)](https://jitpack.io/#Radzdevteam/Security)

## How to Include
### Step 1. Add the repository to your project settings.gradle:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
   ```

### Step 2. Add the dependency
```groovy
dependencies {
     implementation ("com.github.Radzdevteam:Security:1.0")
}

   ```

## Usage

In your `MainActivity`, add the following code:
```kotlin
  class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Set the expected SHA1 hash for app signature
        val cs = Security(this)
        cs.setSHA1("f6:7b:4a:7f:92:0b:ae:ad:ef:79:4a:ca:d8:18:55:df:b7:99:0d:9e")

        // Construct the byte arrays for APP_NAME and PACKAGE_NAME
        val appNameBytes = byteArrayOf(115, 101, 99, 117, 114, 105, 116, 121)
        val packageNameBytes = byteArrayOf(
                99, 111, 109, 46, 114, 97, 100, 122, 100, 101, 118, 46, 115, 101, 99, 117, 114, 105, 116, 121
        )

        // Pass the byte arrays to checkAppIntegrity
        cs.checkAppIntegrity(appNameBytes, packageNameBytes)

        // Check if the signature matches
        cs.check()
    }
}
   ```
