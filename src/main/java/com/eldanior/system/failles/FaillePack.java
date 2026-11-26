package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Instances FIXES des failles, modifiables a la main (demande de l'utilisateur, 2026-11-23). Le jeu refuse d'editer une instance
 * rangee dans un jar (« assetsImmutable ») : les modeles d'instance vivent donc dans un DOSSIER, Mods/EldaniorFailles (a cote du
 * jar du mod), enregistre comme pack d'assets au demarrage. Au premier lancement, le mod y copie les modeles livres dans le jar
 * (resources/failles-pack/Server/Instances/...) ; ensuite il n'y touche plus : les retouches faites en jeu (panneau admin ›
 * Failles › Editer, ou /instances edit load <nom>) y sont sauvegardees et servent a chaque faille ouverte.
 * Pour livrer les retouches avec le mod : scripts/failles/sync_pack.sh (dossier -> resources/failles-pack).
 */
public final class FaillePack {

    private FaillePack() {}

    static final String PACK = "Eldanior:EldaniorFailles", JAR_DIR = "failles-pack/";
    private static Path root;

    public static Path root() { return root; }

    /** Dossier du pack : Mods/EldaniorFailles (dossier du jar du mod), sinon le dossier de donnees du mod. */
    private static Path locate(Path dataDir) {
        try {
            Path jar = Path.of(FaillePack.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(jar) && jar.getParent() != null) return jar.getParent().resolve("EldaniorFailles");
        } catch (Exception ignored) { }
        return dataDir.resolve("EldaniorFailles");
    }

    /** Copie les modeles manquants depuis le jar (jamais par-dessus des retouches) puis enregistre le pack. */
    static void install(JavaPlugin plugin, Path dataDir) {
        root = locate(dataDir);
        try {
            Files.createDirectories(root.resolve("Server").resolve("Instances"));
            Path jar = Path.of(FaillePack.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            int copied = 0;
            if (Files.isRegularFile(jar)) try (ZipFile zip = new ZipFile(jar.toFile())) {
                // (re)installe un modele absent, vide, ou plus ancien que celui du mod (.version) et jamais retouche dans l'editeur
                // des failles (.balises) : les retouches de l'admin ne sont jamais ecrasees
                java.util.Set<String> missing = new java.util.HashSet<>();
                for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements(); ) {
                    ZipEntry e = en.nextElement();
                    String[] parts = e.getName().split("/");
                    if (parts.length != 5 || !(parts[0] + "/").equals(JAR_DIR) || !parts[4].equals(".version")) continue;
                    String name = parts[3];
                    int jarVersion = Integer.parseInt(new String(zip.getInputStream(e).readAllBytes()).trim());
                    Path local = root.resolve("Server").resolve("Instances").resolve(name).resolve(".version");
                    int localVersion = 0;
                    try { if (Files.exists(local)) localVersion = Integer.parseInt(Files.readString(local).trim()); } catch (Exception ignored) { }
                    // 2026-11-23 (demande de l'utilisateur : « ne touche pas a l'instance ») : un modele ABSENT ;
                    // 2026-11-24 : ou un modele JAMAIS RETOUCHE (pas de .retouche) dont le mod livre une version plus recente
                    // (la v1 de la faille D, installee puis abandonnee, demandait une meteo supprimee : faille impossible a ouvrir)
                    Path dir = root.resolve("Server").resolve("Instances").resolve(name);
                    if (!Files.exists(dir.resolve("instance.bson"))
                            || (jarVersion > localVersion && !Files.exists(dir.resolve(".retouche")))) missing.add(name);
                }
                for (String name : missing) {                                                        // ancien modele : chunks retires
                    Path dir = root.resolve("Server").resolve("Instances").resolve(name);
                    for (String sub : new String[]{"chunks", "resources"}) {
                        Path p = dir.resolve(sub);
                        if (Files.exists(p)) try (var walk = Files.walk(p)) {
                            for (Path q : walk.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(q);
                        }
                    }
                    for (String f : new String[]{"config.json", "config.json.bak", "generateurs.json", "boss.json", "spawners.json", ".balises"})
                        Files.deleteIfExists(dir.resolve(f));
                    if (Files.exists(dir)) EldaniorLogger.info("[Failles] modele " + name + " remplace par la version du mod");
                }
                for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements(); ) {
                    ZipEntry e = en.nextElement();
                    String[] parts = e.getName().split("/");
                    if (e.isDirectory() || parts.length < 5 || !(parts[0] + "/").equals(JAR_DIR) || !missing.contains(parts[3])) continue;
                    Path out = root.resolve(e.getName().substring(JAR_DIR.length()));
                    Files.createDirectories(out.getParent());
                    try (InputStream in = zip.getInputStream(e)) { Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
                    copied++;
                }
            }
            // manifeste propre (sinon le pack porte le nom du mod et la liste /instances montre deux dossiers « EldaniorSystem »)
            var m = plugin.getManifest();
            var manifest = new com.hypixel.hytale.common.plugin.PluginManifest("Eldanior", "EldaniorFailles", m.getVersion(),
                    "Failles-donjons : instances modifiables", java.util.List.of(), "", null, m.getServerVersion(),
                    java.util.Map.of(), java.util.Map.of(), java.util.Map.of(), java.util.List.of(), false);
            boolean ok = AssetModule.get().registerPack(PACK, root, manifest, AssetPack.PackSource.RUNTIME);
            EldaniorLogger.info("[Failles] modeles d'instance : " + root + (copied > 0 ? " (" + copied + " fichier(s) installes)" : "")
                    + (ok ? "" : " — pack NON enregistre"));
        } catch (Exception e) {
            EldaniorLogger.error("[Failles] installation des modeles d'instance", e);
        }
    }

    /** Dossier du modele d'une instance (dans le pack). */
    static Path instanceDir(String name) { return root == null ? null : root.resolve("Server").resolve("Instances").resolve(name); }

    /** Le modele a deja ses chunks (construit et/ou retouche a la main) : on ne colle plus le plan a l'ouverture. */
    static boolean prebuilt(String name) {
        Path dir = instanceDir(name);
        if (dir == null) return false;
        try (var s = Files.list(dir.resolve("chunks"))) { return s.findAny().isPresent(); }
        catch (Exception e) { return false; }
    }

    /** Boss choisi pour un modele (poste de PNJ « Boss de donjon » pose pendant l'edition) : boss.json du dossier, coordonnees
     *  relatives a l'origine du donjon. Sans ce fichier, la faille prend le boss du plan. */
    public record TemplateBoss(String role, String name, int level, int x, int y, int z) {}

    static TemplateBoss boss(String name) {
        Path dir = instanceDir(name);
        if (dir == null || !Files.exists(dir.resolve("boss.json"))) return null;
        try { return new com.google.gson.Gson().fromJson(Files.readString(dir.resolve("boss.json")), TemplateBoss.class); }
        catch (Exception e) { EldaniorLogger.error("[Failles] boss du modele " + name, e); return null; }
    }

    static boolean setBoss(String name, TemplateBoss b) {
        Path dir = instanceDir(name);
        if (dir == null) return false;
        try {
            if (b == null) Files.deleteIfExists(dir.resolve("boss.json"));
            else Files.writeString(dir.resolve("boss.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(b));
            return true;
        } catch (Exception e) { EldaniorLogger.error("[Failles] boss du modele " + name, e); return false; }
    }

    /** Le modele a recu les balises d'apparition du plan (fichier temoin .balises dans son dossier). */
    /** Le modele a ete retouche dans l'editeur des failles (« Terminer l'edition ») : jamais remplace. */
    static boolean edited(String name) {
        Path dir = instanceDir(name);
        return dir != null && Files.exists(dir.resolve(".retouche"));
    }

    static void markEdited(String name) {
        try { Files.writeString(instanceDir(name).resolve(".retouche"), "retouche par l'admin"); }
        catch (Exception e) { EldaniorLogger.error("[Failles] temoin de retouche", e); }
    }

    static boolean hasMarkers(String name) {
        Path dir = instanceDir(name);
        return dir != null && Files.exists(dir.resolve(".balises"));
    }

    static void markMarkers(String name) {
        try { Files.writeString(instanceDir(name).resolve(".balises"), "balises du plan posees"); }
        catch (Exception e) { EldaniorLogger.error("[Failles] temoin des balises", e); }
    }

    /** Efface les chunks du modele : la prochaine edition (ou faille) recolle le plan d'origine. */
    static boolean reset(String name) {
        Path dir = instanceDir(name);
        if (dir == null) return false;
        try {
            for (String sub : new String[]{"chunks", "resources"}) {
                Path p = dir.resolve(sub);
                if (!Files.exists(p)) continue;
                try (var walk = Files.walk(p)) {
                    for (Path q : walk.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(q);
                }
            }
            Files.deleteIfExists(dir.resolve("config.json"));
            Files.deleteIfExists(dir.resolve(".balises"));
            Files.deleteIfExists(dir.resolve(".retouche"));
            Files.deleteIfExists(dir.resolve("spawners.json"));
            Files.deleteIfExists(dir.resolve("generateurs.json"));                           // reglages du plan au prochain montage
            Files.deleteIfExists(dir.resolve("boss.json"));                                  // boss du plan
            Files.deleteIfExists(dir.resolve("config.json.bak"));
            return true;
        } catch (Exception e) {
            EldaniorLogger.error("[Failles] remise a zero du modele " + name, e);
            return false;
        }
    }
}
