
# Addon API

BreweryX supports addons that can add new features to the plugin.
This can be new brewing recipes, custom items, or other features.
This section documents how to create an addon for BreweryX.

## Creating an addon

After setting up your project, you'll need a main class that extends `BreweryAddon`
and implements the `onAddonEnable` method.

=== "Java"

    ``` java
    import com.dre.brewery.api.addons.BreweryAddon;
    import com.dre.brewery.api.addons.AddonInfo;    
    
    @AddonInfo(name = "MyAddon", version = "1.0", author = "Jonah")
    public class MyAddon extends BreweryAddon {
        @Override
        public void onAddonEnable() {
            // Code which is executed when MyAddon is enabled
        }

        @Override
        public void onAddonDisable() {
            // Code which is executed when BreweryX is disabled
        }

        @Override
        public void onBreweryReload() {
            // Code which is executed when `/breweryx reload` is executed
        }
    }
    ```
=== "Kotlin"

    ``` kotlin
    import com.dre.brewery.api.addons.BreweryAddon
    import com.dre.brewery.api.addons.AddonInfo

    @AddonInfo(name = "MyAddon", version = "1.0", author = "Jonah")
    class MyAddon : BreweryAddon() {
        override fun onAddonEnable() {
            // Code which is executed when MyAddon is enabled
        }

        override fun onAddonDisable() {
            // Code which is executed when BreweryX is disabled
        }

        override fun onBreweryReload() {
            // Code which is executed when `/breweryx reload` is executed
        }
    }
    ```

## Addon Commands

Addon commands should be registered through the `registerCommand` method in the `onAddonEnable` method.
Addon commands will appear as subcommands for the `/breweryx` command in-game.

=== "Java"

    ``` java
    public class MyCommand implements AddonCommand {
        @Override
        public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
            sender.sendMessage("Hello, from MyCommand!");
        }

        @Override
        public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
            return null;
        }

        @Override
        public String getPermission() {
            return "brewery.myaddon.command";
        }

        @Override
        public boolean playerOnly() {
            return false;
        }
    }

    @Override
    public void onAddonEnable() {
        registerCommand("mycommand", new MyCommand());
    }
    ```

=== "Kotlin"

    ``` kotlin
    class MyCommand : AddonCommand {
        override fun execute(breweryPlugin: BreweryPlugin, lang: Lang, sender: CommandSender, label: String, args: Array<String>) {
            sender.sendMessage("Hello, from MyCommand!")
        }

        override fun tabComplete(breweryPlugin: BreweryPlugin, sender: CommandSender, label: String, args: Array<String>): List<String>? {
            return null
        }

        override fun getPermission(): String {
            return "brewery.myaddon.command"
        }

        override fun playerOnly(): Boolean {
            return false
        }
    }

    override fun onAddonEnable() {
        registerCommand("mycommand", MyCommand())
    }
    ```

## Events in addons

Events in addons should be declared the same way as they are in normal Bukkit plugins.
The only difference being, is that you must register them through your addons `registerListener`
method in the `onAddonEnable` method.

=== "Java"

    ``` java
    public class MyListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            event.getPlayer().sendMessage("Hello, from MyListener!");
        }
    }

    @Override
    public void onAddonEnable() {
        registerListener(new MyListener());
    }
    ```

=== "Kotlin"

    ``` kotlin
    class MyListener : Listener {
        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            event.player.sendMessage("Hello, from MyListener!")
        }
    }

    override fun onAddonEnable() {
        registerListener(MyListener())
    }
    ```

## Configuration files in addons

Addons support configuration files using Okaeri config, 
which is a powerful and easy-to-use configuration library. 

Lombok is recommended for Java developers to reduce boilerplate code.

=== "Java"

    ``` java
    @OkaeriConfigFile(fileName = "addon-config.yml")
    @Getter @Setter
    public class MyConfig extends AddonConfigFile {
        public String message = "Hello, from MyConfig!";
    }

    @Override
    public void onAddonEnable() {
        MyConfig config = getAddonConfigManager().getConfig(MyConfig.class);
        getLogger().info(config.getMessage());
    }
    ```

=== "Kotlin"

    ``` kotlin
    @OkaeriConfigFile(fileName = "addon-config.yml")
    class MyConfig : AddonConfigFile() {
        @JvmField
        var message: String = "Hello, from MyConfig!"
    }

    override fun onAddonEnable() {
        val config = addonConfigManager.getConfig(MyConfig::class.java)
        logger.info(config.message)
    }
    ```


# External Plugin API

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
