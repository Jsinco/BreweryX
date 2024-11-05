
# API

Для Java-разработчиков: используйте любой выпущенный JAR-файл локально или, что предпочтительнее, используйте JitPack:

## Maven

```XML
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>

<dependency>
   <groupId>com.github.Jsinco</groupId>
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
        implementation 'com.github.Jsinco:BreweryX:VERSION'
    }
    ```

=== "Gradle (KTS)"

    ``` kotlin
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        implementation("com.github.Jsinco:BreweryX:VERSION")
    }
    ```

!!! warning

    Не забудьте заменить VERSION на версию [доступную на JitPack](https://jitpack.io/#Jsinco/BreweryX#releasesLink).

Вам также нужно добавить зависимость в файл `plugin.yml` или `paper-plugin.yml`. Это гарантирует, что BreweryX загрузится перед вашим плагином.

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
