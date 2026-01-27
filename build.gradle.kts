import org.gradle.internal.os.OperatingSystem

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
    val replaceProperties = mapOf(
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "plugin_group" to (project.findProperty("plugin_group") ?: project.group),
        "plugin_description" to (project.findProperty("plugin_description") ?: "No description"),
        "plugin_author" to (project.findProperty("plugin_author") ?: "Me"),
        "plugin_website" to (project.findProperty("plugin_website") ?: ""),
        "plugin_main_entrypoint" to (project.findProperty("plugin_main_entrypoint") ?: ""),

        // --- C'est celle qui manquait ---
        "server_version" to (project.findProperty("server_version") ?: "*")
    )

    filesMatching(listOf("**/*.json", "manifest.json")) {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Javadoc> {
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}