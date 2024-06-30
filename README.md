# Radz App Security
[![](https://jitpack.io/v/Radzdevteam/Security.svg)](https://jitpack.io/#Radzdevteam/Security)

## How to Include
### Step 1. Add the repository to your project settings.gradle:
```groovy
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
```groovy
   val cs = Security(this)
   cs.setSHA1("f6:7b:4a:7f:92:0b:ae:ad:ef:79:4a:ca:d8:18:55:df:b7:99:0d:9e")
   cs.check()
   ```

Imports
```groovy
import com.radzdev.security.Security
   ```
