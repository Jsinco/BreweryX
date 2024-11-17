
# API

For Java developers, use any release jar locally or preferably, use JitPack:

## Maven

```XML
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>

<dependency>
   <groupId>com.github.BreweryTeam</groupId>
   <artifactId>BreweryX</artifactId>
   <version>VERSION</version>
   <scope>provided</scope>
</dependency>
```

## Gradle

=== "Gradle (Groovy)"

    ``` groovy
    repositories {
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        implementation 'com.github.BreweryTeam:BreweryX:VERSION'
    }
    ```

=== "Gradle (KTS)"

    ``` kotlin
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        implementation("com.github.BreweryTeam:BreweryX:VERSION")
    }
    ```

!!! warning

    Don't forget to replace `VERSION` with a version [available on JitPack](https://jitpack.io/#BreweryTeam/BreweryX#releasesLink).

You also need to add a dependency in the `plugin.yml` or `paper-plugin.yml` file. This ensures BreweryX loads before your plugin.

=== "plugin.yml"

    ``` yaml
    depend: [BreweryX]
    ```

=== "paper-plugin.yml"

    ``` yaml
    dependencies:
        server:
            BreweryX:
                load: BEFORE
                required: true
                join-classpath: true
    ```
