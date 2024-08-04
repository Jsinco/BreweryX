import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val langVersion: Int = 17

group = "com.dre.brewery"
version = "3.2.5-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io") // GriefPrevention, SlimeFun, PlaceholderAPI
    maven("https://repo.md-5.net/content/groups/public/") // Bungee
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault
    maven("https://maven.enginehub.org/repo/") // WorldEdit, WorldGuard
    maven("https://ci.ender.zone/plugin/repository/everything/") // LWC Extended
    maven("https://repo.minebench.de/") // ChestShop
    maven("https://repo.codemc.org/repository/maven-public/") // BlockLocker
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") // MythicLib (MMOItems)
    maven("https://repo.projectshard.dev/repository/releases/") // Shopkeepers
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
}

// TODO: Figure out exclusions because this is a mess.
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("net.milkbowl.vault:VaultAPI:1.6")
    compileOnly("com.sk89q:worldguard:6.1") // https://dev.bukkit.org/projects/worldedit/files
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT") // https://dev.bukkit.org/projects/worldedit/files
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0-SNAPSHOT") // https://dev.bukkit.org/projects/worldguard/files
    compileOnly("com.griefcraft.lwc:LWCX:2.2.9-dev") {
        exclude("org.bstats", "bstats-bukkit")
    } // https://www.spigotmc.org/resources/lwc-extended.69551/history
    compileOnly("com.github.TechFortress:GriefPrevention:16.18") // https://www.spigotmc.org/resources/griefprevention.1884/history
    compileOnly("de.diddiz:logblock:1.16.5.1") // https://www.spigotmc.org/resources/logblock.67333/history
    compileOnly("com.github.Slimefun:Slimefun4:RC-35") // https://github.com/Slimefun/Slimefun4/releases
    compileOnly("io.lumine:MythicLib-dist:1.6-SNAPSHOT") // https://www.spigotmc.org/resources/mythiclib.90306/history
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2") // https://github.com/ChestShop-authors/ChestShop-3/releases
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.3.0") // https://www.spigotmc.org/resources/towny-advanced.72694/history
    compileOnly("com.nisovin.shopkeepers:ShopkeepersAPI:2.18.0") // https://www.spigotmc.org/resources/shopkeepers.80756/history
    compileOnly("nl.rutgerkok:blocklocker:1.10.4") // https://www.spigotmc.org/resources/blocklocker.3268/history
    compileOnly("me.clip:placeholderapi:2.11.5") // https://www.spigotmc.org/resources/placeholderapi.6245/history

    implementation("org.jetbrains:annotations:16.0.2") // https://www.jetbrains.com/help/idea/annotating-source-code.html
    implementation("com.github.Anon8281:UniversalScheduler:0.1.3") // https://github.com/Anon8281/UniversalScheduler
    //implementation("org.bstats:bstats-bukkit:3.0.2") // https://bstats.org/getting-started/include-metrics

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}



tasks {
    build {
        dependsOn(shadowJar)
        finalizedBy("kotlinReducedJar")
    }

    jar {
        enabled = false // Shadow produces our jar files
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
    test {
        useJUnitPlatform()
    }

    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version.toString()),
            "beginToken" to "\${",
            "endToken" to "}"
        ))
    }

    shadowJar {
        //relocate("org.bstats", "com.dre.brewery.integration.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.dre.brewery.integration.universalScheduler")

        archiveClassifier.set("")
    }


    // Kotlin Reduced Jars
    register<Copy>("kotlinReducedResources") {
        from("src/main/resources")
        into("$buildDir/kt-reduced")
        doLast {
            val pluginFile = file("$buildDir/kt-reduced/plugin.yml")
            var content = pluginFile.readText()
            content = content.replace("libraries: ['org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10']", "")
            pluginFile.writeText(content)
        }
    }

    register<Jar>("kotlinReducedJar") {
        dependsOn("kotlinReducedResources")
        from("$buildDir/kt-reduced")
        archiveClassifier.set("KtReduced")
    }


}



java {
    toolchain.languageVersion = JavaLanguageVersion.of(langVersion)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}



