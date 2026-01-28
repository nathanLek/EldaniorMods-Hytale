import org.gradle.internal.os.OperatingSystem

import java.nio.file.Files

plugins {
    java
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
}

group = "com.eldanior"
version = "1.0.0"

repositories {
    mavenCentral()
}

// --- CONFIGURATION DU CHEMIN HYTALE ---
val hytaleHome: String by project.extra(fun(): String {
    if (project.hasProperty("hytale_home")) {
        return project.findProperty("hytale_home") as String
    }
    val os = OperatingSystem.current()
    val home = System.getProperty("user.home")
    return when {
        os.isWindows -> "$home/AppData/Roaming/Hytale"
        os.isMacOsX -> "$home/Library/Application Support/Hytale"
        os.isLinux -> {
            val flatpak = file("$home/.var/app/com.hypixel.HytaleLauncher/data/Hytale")
            if (flatpak.exists()) flatpak.absolutePath else "$home/.local/share/Hytale"
        }
        else -> throw GradleException("Unsupported OS")
    }
})

// --- CORRECTION MAJEURE ICI ---
// On récupère le 'patchline' depuis gradle.properties (ex: release, live, sartre)
val patchline = project.findProperty("patchline") as String? ?: "live"

// On construit le chemin vers le fichier .jar
val serverJarPath = "$hytaleHome/install/$patchline/package/game/latest/Server/HytaleServer.jar"

// Debug : On affiche dans la console où Gradle cherche le fichier
println("🔍 Recherche de HytaleServer.jar ici :")
println("   👉 $serverJarPath")

if (!file(serverJarPath).exists()) {
    println("⚠️ ERREUR : Le fichier HytaleServer.jar est introuvable !")
    println("   Vérifiez que le dossier '$patchline' existe bien dans '$hytaleHome/install/'")
}

dependencies {
    // Import du serveur Hytale
    implementation(files(serverJarPath))

    // Corrections pour les annotations manquantes
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.jetbrains:annotations:24.1.0")
}

java {
    toolchain {
        // Assurez-vous d'avoir Java 21 installé, sinon mettez 17
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

// --- TÂCHES AUTOMATIQUES ---
tasks.withType<ProcessResources> {
    // AJOUT DE CETTE LIGNE POUR RÉGLER L'ERREUR DE DOUBLON
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val replaceProperties = mapOf(
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "plugin_group" to (project.findProperty("plugin_group") ?: project.group),
        "plugin_description" to (project.findProperty("plugin_description") ?: "No description"),
        "plugin_author" to (project.findProperty("plugin_author") ?: "Me"),
        "plugin_website" to (project.findProperty("plugin_website") ?: ""),
        "plugin_main_entrypoint" to (project.findProperty("plugin_main_entrypoint") ?: ""),
        "server_version" to (project.findProperty("server_version") ?: "*"),
        "includes_pack" to (project.findProperty("includes_pack") ?: true)
    )

    filesMatching(listOf("**/*.json", "manifest.json")) {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

/// --- TÂCHE DE LANCEMENT DU SERVEUR (VERSION COMPATIBLE ZIP) ---
tasks.register<Exec>("runServer") {
    group = "Hytale"
    description = "Lance le serveur (Stratégie: Assets dans /mods)."
    dependsOn("jar")

    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    val javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }.get()
    executable(javaLauncher.executablePath.asFile.absolutePath)

    val runDir = file("run")
    val modsDir = runDir.resolve("mods")
    val serverJar = file(serverJarPath)
    val gameRootDir = serverJar.parentFile.parentFile
    val assetsZip = gameRootDir.resolve("Assets.zip")

    // STRATÉGIE : On met les assets dans 'mods/Hytale'
    // Ainsi, le nom du dossier est "Hytale" (ce qui plait au manifest)
    // Et le serveur le scanne automatiquement.
    val destAssets = modsDir.resolve("Hytale")

    doFirst {
        println(">>> PRÉPARATION DU LANCEMENT <<<")

        if (!modsDir.exists()) modsDir.mkdirs()

        // A. Copie du Mod (Votre code)
        val pluginJar = tasks.jar.get().archiveFile.get().asFile
        if (pluginJar.exists()) {
            copy { from(pluginJar); into(modsDir) }
        }

        // B. Nettoyage des anciennes tentatives qui font planter le serveur
        val badFolder = runDir.resolve("HytaleAssets")
        if (badFolder.exists()) {
            println("🧹 Suppression du dossier 'HytaleAssets' racine (cause de conflits)...")
            badFolder.deleteRecursively()
        }

        // C. Extraction des Assets dans mods/Hytale
        if (!destAssets.exists()) {
            println("🔍 Extraction des assets vers 'mods/Hytale'...")
            if (assetsZip.exists()) {
                println("📦 Assets.zip trouvé ! Extraction en cours...")

                copy {
                    from(zipTree(assetsZip))
                    into(destAssets)
                }

                // On supprime quand même le hash par sécurité, au cas où
                val hashFile = destAssets.resolve("CommonAssetsIndex.hashes")
                if (hashFile.exists()) hashFile.delete()

                println("✅ Assets installés dans le dossier des mods.")
            } else {
                println("⚠️ ATTENTION : Assets.zip introuvable !")
            }
        } else {
            println("✅ Assets déjà présents dans mods/Hytale.")
        }

        println(">>> LANCEMENT DU SERVEUR <<<")
    }

    workingDir = runDir
    args("-Xmx1G", "-Xms1G", "-jar", serverJar.absolutePath)
    standardInput = System.`in`
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(file("/Users/nathanlecat/Library/Application Support/Hytale/UserData/Mods"))
    from("src/main/resources") {
        include("**/*")
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Javadoc> {
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

