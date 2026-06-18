package com.eldanior.system.config;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Utilitaire d'ecriture atomique pour fichiers .properties.
 * Ecrit dans un fichier temporaire .tmp puis fait un move atomique,
 * avec backup .bak du fichier existant avant ecrasement.
 */
public final class PersistenceUtils {

    private PersistenceUtils() {}

    /**
     * Ecrit les properties de maniere atomique avec backup.
     * 1. Copie target vers target.bak (si target existe)
     * 2. Ecrit dans target.tmp
     * 3. Move atomique target.tmp -> target
     *
     * @param target  le fichier .properties cible
     * @param props   les proprietes a ecrire
     * @param comment le commentaire d'en-tete du fichier
     * @throws IOException en cas d'erreur d'ecriture
     */
    public static void writeAtomicWithBackup(Path target, Properties props, String comment) throws IOException {
        // S'assurer que le repertoire parent existe
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tmpFile = target.resolveSibling(target.getFileName() + ".tmp");
        Path bakFile = target.resolveSibling(target.getFileName() + ".bak");

        // Ecrire dans le fichier temporaire
        try (OutputStream out = Files.newOutputStream(tmpFile)) {
            props.store(out, comment);
        }

        // Backup de l'existant
        if (Files.exists(target)) {
            try {
                Files.copy(target, bakFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("[PersistenceUtils] Backup echoue pour " + target + ": " + e.getMessage());
                // On continue quand meme - le backup est best-effort
            }
        }

        // Move atomique du .tmp vers la cible
        Files.move(tmpFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
