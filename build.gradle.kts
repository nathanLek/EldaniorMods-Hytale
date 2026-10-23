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
    implementation("com.google.guava:guava:31.1-jre")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    // Supprime les warnings pour les méthodes Hytale deprecated sans remplacement
    // (getPlayerRef, getHotbar, getStorage, getActiveHotbarSlot)
    options.compilerArgs.addAll(listOf("-Xlint:-removal", "-Xlint:-deprecation"))
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

    filesMatching("manifest.json") {
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }.get()
    executable(javaLauncher.executablePath.asFile.absolutePath)

    val runDir = file("run")
    val modsDir = runDir.resolve("mods")
    val serverJar = file(serverJarPath)
    val gameRootDir = serverJar.parentFile.parentFile
    val assetsZip = gameRootDir.resolve("Assets.zip")

    // STRATÉGIE (0.7.x) : on pointe '--assets' directement sur le Assets.zip du jeu.
    // Le serveur accepte un zip comme pack de base (DIR_OR_ZIP) et le charge en
    // lecture seule — c'est lui qui fournit les cosmétiques, modèles, particules, etc.
    // Plus besoin d'extraire 3,9 Go dans mods/Hytale (ce qui cassait la validation
    // des cosmétiques et chargeait les assets de test cassés en 0.7.x).

    doFirst {
        println(">>> PRÉPARATION DU LANCEMENT <<<")

        if (!modsDir.exists()) modsDir.mkdirs()

        // A. Copie du Mod dans mods/
        val pluginJar = tasks.jar.get().archiveFile.get().asFile
        if (pluginJar.exists()) {
            copy { from(pluginJar); into(modsDir) }
        }

        // B. Nettoyage des anciennes extractions (contournement 0.6.x) qui font
        //    désormais planter le boot en 0.7.x.
        listOf(runDir.resolve("HytaleAssets"), modsDir.resolve("Hytale")).forEach { stale ->
            if (stale.exists()) {
                println("🧹 Suppression de l'ancienne extraction '${stale.name}'...")
                stale.deleteRecursively()
            }
        }

        if (!assetsZip.exists()) {
            println("⚠️ ATTENTION : Assets.zip introuvable : $assetsZip")
        } else {
            println("📦 Assets.zip utilisé : $assetsZip")
        }

        println(">>> LANCEMENT DU SERVEUR <<<")
    }

    workingDir = runDir
    args("-Xmx1G", "-Xms1G", "-jar", serverJar.absolutePath, "--assets", assetsZip.absolutePath)
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

